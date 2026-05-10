#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASSWORD="${PHARMACY_TEST_PASSWORD:-vikash123}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

CUSTOMER_TOKEN=""
ADMIN_TOKEN=""
END_USER_TOKEN=""
NEW_MEDICINE_ID=""
NEW_ADDRESS_ID=""
NEW_CART_ITEM_ID=""
NEW_ORDER_ID=""
NEW_PRESCRIPTION_ID=""
NEW_USER_ID=""

request() {
  local name="$1"
  local method="$2"
  local path="$3"
  local expected="$4"
  local token="${5:-}"
  local body="${6:-}"
  local extra="${7:-}"
  local out="$tmp_dir/response.json"
  local headers=(-H "Accept: application/json")

  if [[ -n "$token" ]]; then
    headers+=(-H "Authorization: Bearer $token")
  fi

  if [[ -n "$body" ]]; then
    headers+=(-H "Content-Type: application/json")
  fi

  # shellcheck disable=SC2086
  local status
  status="$(curl -sS -o "$out" -w "%{http_code}" -X "$method" "${headers[@]}" ${body:+--data "$body"} $extra "$BASE_URL$path")"

  if [[ ! ",$expected," == *",$status,"* ]]; then
    echo "FAIL $method $path ($name): expected $expected, got $status" >&2
    sed -n '1,12p' "$out" >&2
    return 1
  fi

  echo "PASS $method $path ($status) - $name" >&2
}

json_value() {
  jq -r "$1 // empty" "$tmp_dir/response.json"
}

login() {
  local email="$1"
  local body
  body="$(jq -nc --arg email "$email" --arg password "$PASSWORD" '{email:$email,password:$password}')"
  request "login $email" POST "/api/auth/login" "200" "" "$body"
  json_value '.token'
}

CUSTOMER_TOKEN="$(login "customer1@pharmacy.local")"
ADMIN_TOKEN="$(login "admin@pharmacy.local")"

request "customer profile" GET "/api/auth/me" "200" "$CUSTOMER_TOKEN"
request "admin profile" GET "/api/auth/me" "200" "$ADMIN_TOKEN"

unique="$(date +%s%N)"
mobile_suffix="${unique: -8}"
signup_body="$(jq -nc \
  --arg email "smoke-$unique@pharmacy.local" \
  --arg mobile "98$mobile_suffix" \
  '{name:"Smoke Test User",email:$email,mobile:$mobile,password:"vikash123",address:"Smoke Test Address"}')"
request "signup disposable customer" POST "/api/auth/signup" "200" "" "$signup_body"
NEW_USER_ID="$(json_value '.userId')"
END_USER_TOKEN="$(json_value '.token')"

request "list admin users" GET "/api/admin/users" "200" "$ADMIN_TOKEN"
request "get admin user" GET "/api/admin/users/$NEW_USER_ID" "200" "$ADMIN_TOKEN"
request "update admin user" PUT "/api/admin/users/$NEW_USER_ID" "200" "$ADMIN_TOKEN" '{"role":"CUSTOMER","active":true}'

request "list categories" GET "/api/catalog/categories" "200" ""
request "get category" GET "/api/catalog/categories/101" "200" ""
request "list medicines" GET "/api/catalog/medicines?page=0&size=10" "200" ""
request "filter medicines" GET "/api/catalog/medicines?categoryId=101&search=Paracetamol&page=0&size=10" "200" ""
request "get medicine" GET "/api/catalog/medicines/1001" "200" ""

medicine_body='{"name":"Smoke Test Medicine","description":"Temporary smoke-test medicine","categoryId":101,"manufacturer":"Smoke Labs","price":12.5,"stock":3,"requiresPrescription":false,"imageUrl":"/images/medicines/smoke.png","dosageInfo":"As directed","sideEffects":"None"}'
request "create medicine" POST "/api/catalog/medicines" "200,201" "$ADMIN_TOKEN" "$medicine_body"
NEW_MEDICINE_ID="$(json_value '.id')"
request "update medicine" PUT "/api/catalog/medicines/$NEW_MEDICINE_ID" "200" "$ADMIN_TOKEN" '{"name":"Smoke Test Medicine Updated","description":"Temporary smoke-test medicine","categoryId":101,"manufacturer":"Smoke Labs","price":13.5,"stock":4,"requiresPrescription":false,"imageUrl":"/images/medicines/smoke.png","dosageInfo":"As directed","sideEffects":"None"}'

request "customer prescriptions" GET "/api/catalog/prescriptions/my" "200" "$END_USER_TOKEN"
printf '%%PDF-1.4\n%% smoke prescription\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%%%EOF\n' > "$tmp_dir/prescription.pdf"
upload_status="$(curl -sS -o "$tmp_dir/response.json" -w "%{http_code}" \
  -H "Authorization: Bearer $END_USER_TOKEN" \
  -F "file=@$tmp_dir/prescription.pdf;type=application/pdf" \
  "$BASE_URL/api/catalog/prescriptions/upload")"
if [[ "$upload_status" != "200" && "$upload_status" != "201" ]]; then
  echo "FAIL POST /api/catalog/prescriptions/upload: expected 200/201, got $upload_status" >&2
  sed -n '1,12p' "$tmp_dir/response.json" >&2
  exit 1
fi
echo "PASS POST /api/catalog/prescriptions/upload ($upload_status) - upload prescription" >&2
NEW_PRESCRIPTION_ID="$(json_value '.id')"
request "get prescription" GET "/api/catalog/prescriptions/$NEW_PRESCRIPTION_ID" "200" "$END_USER_TOKEN"
request "download prescription file" GET "/api/catalog/prescriptions/$NEW_PRESCRIPTION_ID/file" "200" "$END_USER_TOKEN"

request "list addresses" GET "/api/orders/addresses" "200" "$END_USER_TOKEN"
address_body='{"fullName":"Smoke Test User","mobile":"9898989898","addressLine1":"1 Smoke Street","addressLine2":"Near Test Lab","city":"Kolkata","state":"West Bengal","pincode":"700001","default":false}'
request "create address" POST "/api/orders/addresses" "200,201" "$END_USER_TOKEN" "$address_body"
NEW_ADDRESS_ID="$(json_value '.id')"

request "get cart" GET "/api/orders/cart" "200" "$END_USER_TOKEN"
cart_body='{"medicineId":1001,"medicineName":"Paracetamol 650","price":35.0,"quantity":1,"requiresPrescription":false}'
request "add cart item" POST "/api/orders/cart/items" "200,201" "$END_USER_TOKEN" "$cart_body"
NEW_CART_ITEM_ID="$(json_value '.items[-1].id')"
if [[ -n "$NEW_CART_ITEM_ID" ]]; then
  request "update cart item" PUT "/api/orders/cart/items/$NEW_CART_ITEM_ID?quantity=2" "200" "$END_USER_TOKEN"
  request "delete cart item" DELETE "/api/orders/cart/items/$NEW_CART_ITEM_ID" "200,204" "$END_USER_TOKEN"
fi

request "add checkout cart item" POST "/api/orders/cart/items" "200,201" "$END_USER_TOKEN" "$cart_body"
request "start checkout" POST "/api/orders/checkout/start" "200,201" "$END_USER_TOKEN" "$(jq -nc --argjson addressId "$NEW_ADDRESS_ID" '{addressId:$addressId,deliverySlot:"9AM-11AM"}')"
NEW_ORDER_ID="$(json_value '.id')"
request "list orders" GET "/api/orders" "200" "$END_USER_TOKEN"
request "get order" GET "/api/orders/$NEW_ORDER_ID" "200" "$END_USER_TOKEN"
request "pay order" POST "/api/orders/$NEW_ORDER_ID/payment" "200" "$END_USER_TOKEN" "$(jq -nc --argjson orderId "$NEW_ORDER_ID" '{orderId:$orderId,paymentMethod:"UPI",transactionReference:"SMOKE-TXN"}')"
request "order status update" PUT "/api/orders/$NEW_ORDER_ID/status" "200" "$ADMIN_TOKEN" '{"status":"PACKED"}'
request "cancel paid order rejects or succeeds" POST "/api/orders/$NEW_ORDER_ID/cancel" "200,400,409" "$END_USER_TOKEN"

request "admin dashboard" GET "/api/admin/dashboard" "200" "$ADMIN_TOKEN"
request "admin orders" GET "/api/admin/orders" "200" "$ADMIN_TOKEN"
request "admin order status" PUT "/api/admin/orders/$NEW_ORDER_ID/status" "200" "$ADMIN_TOKEN" '{"status":"OUT_FOR_DELIVERY"}'
request "sales report" GET "/api/admin/reports/sales?from=2026-03-01&to=2026-05-10&format=json" "200" "$ADMIN_TOKEN"
request "inventory report" GET "/api/admin/reports/inventory?format=json" "200" "$ADMIN_TOKEN"
request "prescription by order" GET "/api/catalog/prescriptions/order/5003" "200,404" "$ADMIN_TOKEN"

if [[ -n "$NEW_MEDICINE_ID" ]]; then
  request "delete medicine" DELETE "/api/catalog/medicines/$NEW_MEDICINE_ID" "200,204" "$ADMIN_TOKEN"
fi
request "clear cart" DELETE "/api/orders/cart" "200,204" "$END_USER_TOKEN"

echo "All live API smoke checks passed."
