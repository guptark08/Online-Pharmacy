import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BarChart3, ClipboardList, FileText, Home, Package, Plus, Search, ShieldCheck, Trash2, Users } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Navigate, useLocation, useNavigate, useParams } from "react-router-dom";
import { z } from "zod";
import {
  addCartItem,
  cancelOrder,
  cartItemFromMedicine,
  clearCart,
  deleteMedicine,
  getAdminDashboard,
  getAdminOrders,
  getAddresses,
  getCart,
  getCategories,
  getInventoryReport,
  getMedicine,
  getMedicines,
  getOrder,
  getOrders,
  getPrescriptionFile,
  getPrescriptions,
  getSalesReport,
  getUsers,
  login,
  payForOrder,
  removeCartItem,
  saveAddress,
  saveMedicine,
  signup,
  startCheckout,
  updateAdminOrderStatus,
  updateCartItem,
  updateUser,
  uploadPrescription,
} from "../api/pharmacy-api.js";
import { MedicineCard } from "../components/app/MedicineCard.jsx";
import { EmptyState, ErrorState, LoadingState } from "../components/app/States.jsx";
import { Badge, StatusBadge } from "../components/ui/badge.jsx";
import { Button } from "../components/ui/button.jsx";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card.jsx";
import { Field, Input, Select, Textarea } from "../components/ui/form.jsx";
import { asArray, formatCurrency, formatDate, medicineImage, useDefaultMedicineImage } from "../lib/utils.js";
import { useAuthStore } from "../store/auth-store.js";

const paymentMethods = ["UPI", "CARD", "COD", "NET_BANKING"];
const orderStatuses = [
  "PRESCRIPTION_PENDING",
  "PRESCRIPTION_APPROVED",
  "PRESCRIPTION_REJECTED",
  "PAYMENT_PENDING",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "ADMIN_CANCELLED",
];

function useToken() {
  return useAuthStore((state) => state.token);
}

function PageTitle({ icon: Icon, title, text, action }) {
  return (
    <div className="page-title">
      <div>
        <p>{Icon ? <Icon size={18} /> : null} PulseMeds</p>
        <h1>{title}</h1>
        {text ? <span>{text}</span> : null}
      </div>
      {action}
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <Card>
      <CardContent className="stat">
        <span>{label}</span>
        <strong>{value}</strong>
      </CardContent>
    </Card>
  );
}

function ListCard({ items, empty, render }) {
  if (!items.length) return <EmptyState title={empty} />;
  return (
    <Card>
      <CardContent className="table-list">
        {items.map((item) => (
          <div className="table-row" key={item.id}>
            {render(item)}
          </div>
        ))}
      </CardContent>
    </Card>
  );
}

export function LandingPage() {
  const token = useToken();
  if (token) return <Navigate to="/app" replace />;

  return (
    <main className="landing">
      <section className="landing-copy">
        <Badge tone="success">Online Pharmacy</Badge>
        <h1>PulseMeds</h1>
        <p>Search medicines, upload prescriptions, place orders, and manage pharmacy operations from one clean React app.</p>
        <div className="button-row">
          <Button asChild><Link to="/login">Sign in</Link></Button>
          <Button asChild variant="secondary"><Link to="/signup">Create account</Link></Button>
        </div>
      </section>
      <section className="landing-media">
        <img src="/medicine-images/paracetamol-650.svg" alt="Medicine pack" />
      </section>
    </main>
  );
}

const loginSchema = z.object({
  email: z.string().email("Enter a valid email."),
  password: z.string().min(6, "Password must be at least 6 characters."),
});

const signupSchema = loginSchema.extend({
  name: z.string().min(2, "Name is required."),
  mobile: z.string().min(8, "Mobile number is required."),
  address: z.string().min(5, "Address is required."),
});

function AuthPage({ mode }) {
  const isSignup = mode === "signup";
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((state) => state.setSession);
  const form = useForm({
    resolver: zodResolver(isSignup ? signupSchema : loginSchema),
    defaultValues: { name: "", email: "", password: "", mobile: "", address: "" },
  });
  const mutation = useMutation({
    mutationFn: isSignup ? signup : login,
    onSuccess: (session) => {
      setSession(session);
      navigate(location.state?.from || "/app", { replace: true });
    },
  });

  return (
    <main className="auth-page">
      <Card className="auth-card">
        <CardHeader>
          <CardTitle>{isSignup ? "Create account" : "Sign in"}</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="form-stack" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
            {isSignup ? (
              <>
                <Field label="Name" error={form.formState.errors.name?.message}><Input {...form.register("name")} /></Field>
                <Field label="Mobile" error={form.formState.errors.mobile?.message}><Input {...form.register("mobile")} /></Field>
                <Field label="Address" error={form.formState.errors.address?.message}><Textarea {...form.register("address")} /></Field>
              </>
            ) : null}
            <Field label="Email" error={form.formState.errors.email?.message}><Input type="email" {...form.register("email")} /></Field>
            <Field label="Password" error={form.formState.errors.password?.message}><Input type="password" {...form.register("password")} /></Field>
            <ErrorState error={mutation.error} />
            <Button disabled={mutation.isPending}>{mutation.isPending ? "Please wait" : "Continue"}</Button>
          </form>
          <p className="auth-switch">
            {isSignup ? "Already have an account?" : "Need an account?"}{" "}
            <Link to={isSignup ? "/login" : "/signup"}>{isSignup ? "Sign in" : "Sign up"}</Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}

export const LoginPage = () => <AuthPage mode="login" />;
export const SignupPage = () => <AuthPage mode="signup" />;

export function HomePage() {
  const token = useToken();
  const user = useAuthStore((state) => state.user);
  const cart = useQuery({ queryKey: ["cart", token], queryFn: () => getCart(token) });
  const orders = useQuery({ queryKey: ["orders", token], queryFn: () => getOrders(token) });
  const prescriptions = useQuery({ queryKey: ["prescriptions", token], queryFn: () => getPrescriptions(token) });

  if (user?.role === "ADMIN") {
    return (
      <>
        <PageTitle icon={Home} title="Admin dashboard" text="Manage pharmacy operations." />
        <div className="quick-grid">
          <QuickLink to="/app/admin/orders" title="Review orders" text="Update fulfillment and prescription status." />
          <QuickLink to="/app/admin/medicines" title="Manage medicines" text="Add or update catalog items." />
          <QuickLink to="/app/admin/reports" title="View reports" text="Track sales and inventory." />
        </div>
      </>
    );
  }

  return (
    <>
      <PageTitle icon={Home} title="Dashboard" text="Your pharmacy activity at a glance." />
      <div className="stat-grid">
        <Stat label="Cart items" value={cart.data?.totalItems || cart.data?.items?.length || 0} />
        <Stat label="Orders" value={asArray(orders.data).length} />
        <Stat label="Prescriptions" value={asArray(prescriptions.data).length} />
      </div>
      <div className="quick-grid">
        <QuickLink to="/app/catalog" title="Shop medicines" text="Browse catalog and add items." />
        <QuickLink to="/app/account/prescriptions" title="Upload prescription" text="Submit required documents." />
        <QuickLink to="/app/account/orders" title="Track orders" text="Review order status." />
      </div>
    </>
  );
}

function QuickLink({ to, title, text }) {
  return (
    <Card>
      <CardContent>
        <h3>{title}</h3>
        <p className="muted">{text}</p>
        <Button asChild variant="secondary"><Link to={to}>Open</Link></Button>
      </CardContent>
    </Card>
  );
}

export function CatalogPage() {
  const token = useToken();
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState({ search: "", categoryId: "", prescriptionOnly: false, page: 0, size: 12 });
  const categories = useQuery({ queryKey: ["categories"], queryFn: getCategories });
  const medicines = useQuery({ queryKey: ["medicines", filters], queryFn: () => getMedicines(filters) });
  const addToCart = useMutation({
    mutationFn: (medicine) => addCartItem(token, cartItemFromMedicine(medicine)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["cart"] }),
  });
  const list = asArray(medicines.data);
  const page = medicines.data?.page ?? filters.page;
  const totalPages = medicines.data?.totalPages ?? 0;
  const totalElements = medicines.data?.totalElements ?? list.length;
  const isFirstPage = medicines.data?.first ?? page <= 0;
  const isLastPage = medicines.data?.last ?? (totalPages <= 1 || page >= totalPages - 1);
  const shownCount = Math.min((page * filters.size) + list.length, totalElements);
  const updateFilter = (nextFilter) => setFilters((current) => ({ ...current, ...nextFilter, page: 0 }));
  const goToPage = (nextPage) => setFilters((current) => ({ ...current, page: nextPage }));

  return (
    <>
      <PageTitle icon={Search} title="Catalog" text="Find medicines and add them to your cart." />
      <Card className="filter-card">
        <CardContent className="filter-row">
          <Input placeholder="Search medicine" value={filters.search} onChange={(event) => updateFilter({ search: event.target.value })} />
          <Select value={filters.categoryId} onChange={(event) => updateFilter({ categoryId: event.target.value })}>
            <option value="">All categories</option>
            {asArray(categories.data).map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </Select>
          <label className="check-row">
            <input type="checkbox" checked={filters.prescriptionOnly} onChange={(event) => updateFilter({ prescriptionOnly: event.target.checked })} />
            Rx only
          </label>
        </CardContent>
      </Card>
      {medicines.isLoading ? <LoadingState label="Loading medicines" /> : null}
      <ErrorState error={medicines.error || addToCart.error} />
      {list.length ? (
        <>
          <div className="medicine-grid">
            {list.map((medicine) => <MedicineCard key={medicine.id} medicine={medicine} onAddToCart={addToCart.mutate} />)}
          </div>
          <div className="pagination-row">
            <span>
              Showing {shownCount} of {totalElements} medicines
            </span>
            {totalPages > 1 ? (
              <div className="pagination-actions">
                <Button variant="secondary" disabled={isFirstPage || medicines.isFetching} onClick={() => goToPage(Math.max(page - 1, 0))}>Previous</Button>
                <strong>Page {page + 1} of {totalPages}</strong>
                <Button variant="secondary" disabled={isLastPage || medicines.isFetching} onClick={() => goToPage(page + 1)}>Next</Button>
              </div>
            ) : null}
          </div>
        </>
      ) : (
        !medicines.isLoading && <EmptyState title="No medicines found" text="Change filters and try again." />
      )}
    </>
  );
}

export function MedicineDetailPage() {
  const { medicineId } = useParams();
  const token = useToken();
  const queryClient = useQueryClient();
  const medicine = useQuery({ queryKey: ["medicine", medicineId], queryFn: () => getMedicine(medicineId) });
  const addToCart = useMutation({
    mutationFn: () => addCartItem(token, cartItemFromMedicine(medicine.data)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["cart"] }),
  });

  if (medicine.isLoading) return <LoadingState label="Loading medicine" />;
  if (medicine.error) return <ErrorState error={medicine.error} />;

  return (
    <div className="detail-grid">
      <Card><CardContent className="detail-image"><img src={medicineImage(medicine.data)} alt={medicine.data.name} onError={useDefaultMedicineImage} /></CardContent></Card>
      <section>
        <PageTitle title={medicine.data.name} text={medicine.data.manufacturer || "Medicine details"} />
        <p className="detail-text">{medicine.data.description || "No description available."}</p>
        <div className="button-row">
          <Badge tone={medicine.data.requiresPrescription ? "warning" : "success"}>{medicine.data.requiresPrescription ? "Prescription required" : "No prescription needed"}</Badge>
          <Badge tone={medicine.data.stock > 0 ? "success" : "danger"}>{medicine.data.stock} in stock</Badge>
        </div>
        <h2>{formatCurrency(medicine.data.price)}</h2>
        <ErrorState error={addToCart.error} />
        <Button onClick={() => addToCart.mutate()} disabled={addToCart.isPending || medicine.data.stock <= 0}>Add to cart</Button>
      </section>
    </div>
  );
}

export function CartPage() {
  const token = useToken();
  const queryClient = useQueryClient();
  const cart = useQuery({ queryKey: ["cart", token], queryFn: () => getCart(token) });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["cart"] });
  const update = useMutation({ mutationFn: ({ itemId, quantity }) => updateCartItem(token, itemId, quantity), onSuccess: invalidate });
  const remove = useMutation({ mutationFn: (itemId) => removeCartItem(token, itemId), onSuccess: invalidate });
  const clear = useMutation({ mutationFn: () => clearCart(token), onSuccess: invalidate });
  const items = cart.data?.items ?? [];

  return (
    <>
      <PageTitle icon={Package} title="Cart" text="Review items before checkout." />
      {cart.isLoading ? <LoadingState label="Loading cart" /> : null}
      <ErrorState error={cart.error || update.error || remove.error || clear.error} />
      {items.length ? (
        <div className="cart-layout">
          <Card>
            <CardContent className="table-list">
              {items.map((item) => (
                <div className="table-row" key={item.id}>
                  <div><strong>{item.medicineName || item.name}</strong><p className="muted">{formatCurrency(item.price)} each</p></div>
                  <Input className="qty-input" type="number" min="1" value={item.quantity} onChange={(event) => update.mutate({ itemId: item.id, quantity: Number(event.target.value) })} />
                  <strong>{formatCurrency(item.lineTotal || item.totalPrice || item.price * item.quantity)}</strong>
                  <Button variant="ghost" size="icon" onClick={() => remove.mutate(item.id)}><Trash2 size={17} /></Button>
                </div>
              ))}
            </CardContent>
          </Card>
          <Card>
            <CardContent className="summary">
              <span>Total</span>
              <strong>{formatCurrency(cart.data.totalAmount)}</strong>
              <Button asChild><Link to="/app/account/checkout">Checkout</Link></Button>
              <Button variant="outline" onClick={() => clear.mutate()}>Clear cart</Button>
            </CardContent>
          </Card>
        </div>
      ) : (
        !cart.isLoading && <EmptyState title="Your cart is empty" text="Add medicines from the catalog." />
      )}
    </>
  );
}

const addressSchema = z.object({
  recipientName: z.string().min(2, "Name is required."),
  mobile: z.string().min(8, "Mobile is required."),
  line1: z.string().min(4, "Address line is required."),
  city: z.string().min(2, "City is required."),
  state: z.string().min(2, "State is required."),
  pincode: z.string().min(4, "Pincode is required."),
});

export function CheckoutPage() {
  const token = useToken();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [addressId, setAddressId] = useState("");
  const [prescriptionId, setPrescriptionId] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const cart = useQuery({ queryKey: ["cart", token], queryFn: () => getCart(token) });
  const addresses = useQuery({ queryKey: ["addresses", token], queryFn: () => getAddresses(token) });
  const prescriptions = useQuery({ queryKey: ["prescriptions", token], queryFn: () => getPrescriptions(token) });
  const form = useForm({ resolver: zodResolver(addressSchema), defaultValues: { recipientName: "", mobile: "", line1: "", city: "", state: "", pincode: "" } });
  const saveAddressMutation = useMutation({
    mutationFn: (values) => saveAddress(token, values),
    onSuccess: (address) => {
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
      setAddressId(String(address.id));
    },
  });
  const uploadRx = useMutation({
    mutationFn: (file) => uploadPrescription(token, file),
    onSuccess: (prescription) => {
      queryClient.invalidateQueries({ queryKey: ["prescriptions"] });
      setPrescriptionId(String(prescription.id));
    },
  });
  const checkout = useMutation({
    mutationFn: () => startCheckout(token, {
      addressId: Number(addressId),
      paymentMethod,
      ...(needsPrescription ? { prescriptionId: Number(prescriptionId) } : {}),
    }),
    onSuccess: async (order) => {
      if (order.status === "PAYMENT_PENDING" && paymentMethod !== "COD") {
        await payForOrder(token, order.id, paymentMethod);
      }
      queryClient.invalidateQueries({ queryKey: ["cart"] });
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      navigate(`/app/account/orders/${order.id}`);
    },
  });
  const cartItems = cart.data?.items || [];
  const needsPrescription = cartItems.some((item) => item.requiresPrescription);
  const usablePrescriptions = asArray(prescriptions.data).filter((item) => item.status !== "REJECTED");
  const canPlaceOrder = Boolean(addressId) && (!needsPrescription || Boolean(prescriptionId));

  return (
    <>
      <PageTitle icon={ShieldCheck} title="Checkout" text="Choose an address and payment method." />
      <div className="two-column">
        <Card>
          <CardHeader><CardTitle>Delivery address</CardTitle></CardHeader>
          <CardContent className="form-stack">
            <Select value={addressId} onChange={(event) => setAddressId(event.target.value)}>
              <option value="">Select saved address</option>
              {asArray(addresses.data).map((address) => <option key={address.id} value={address.id}>{address.recipientName}, {address.city}</option>)}
            </Select>
            <form className="form-stack" onSubmit={form.handleSubmit((values) => saveAddressMutation.mutate(values))}>
              <Field label="Recipient" error={form.formState.errors.recipientName?.message}><Input {...form.register("recipientName")} /></Field>
              <Field label="Mobile" error={form.formState.errors.mobile?.message}><Input {...form.register("mobile")} /></Field>
              <Field label="Address" error={form.formState.errors.line1?.message}><Textarea {...form.register("line1")} /></Field>
              <div className="form-grid">
                <Field label="City" error={form.formState.errors.city?.message}><Input {...form.register("city")} /></Field>
                <Field label="State" error={form.formState.errors.state?.message}><Input {...form.register("state")} /></Field>
                <Field label="Pincode" error={form.formState.errors.pincode?.message}><Input {...form.register("pincode")} /></Field>
              </div>
              <Button variant="secondary" disabled={saveAddressMutation.isPending}>Save address</Button>
            </form>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>{needsPrescription ? "Prescription review" : "Payment"}</CardTitle></CardHeader>
          <CardContent className="form-stack">
            {cart.isLoading ? <LoadingState label="Checking cart" /> : null}
            {needsPrescription ? (
              <>
                <p className="muted">This cart contains prescription medicines. Upload a prescription or select a saved one. Admin approval is required before payment.</p>
                <Select value={prescriptionId} onChange={(event) => setPrescriptionId(event.target.value)}>
                  <option value="">Select prescription</option>
                  {usablePrescriptions.map((prescription) => (
                    <option key={prescription.id} value={prescription.id}>
                      {prescription.fileName || `Prescription #${prescription.id}`} - {prescription.status}
                    </option>
                  ))}
                </Select>
                <Input type="file" accept="image/jpeg,image/png,application/pdf" onChange={(event) => event.target.files?.[0] && uploadRx.mutate(event.target.files[0])} />
              </>
            ) : null}
            <Select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>
              {paymentMethods.map((method) => <option key={method}>{method}</option>)}
            </Select>
            <ErrorState error={cart.error || prescriptions.error || uploadRx.error || saveAddressMutation.error || checkout.error} />
            <Button disabled={!canPlaceOrder || checkout.isPending || uploadRx.isPending} onClick={() => checkout.mutate()}>
              {needsPrescription ? "Place order for approval" : "Place order"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </>
  );
}

export function ProfilePage() {
  const user = useAuthStore((state) => state.user);
  return (
    <>
      <PageTitle icon={Users} title="Profile" text="Your saved account information." />
      <Card><CardContent className="profile-list">
        <p><span>Name</span><strong>{user?.name || "Not set"}</strong></p>
        <p><span>Email</span><strong>{user?.email}</strong></p>
        <p><span>Mobile</span><strong>{user?.mobile || "Not set"}</strong></p>
        <p><span>Role</span><StatusBadge value={user?.role} /></p>
        <p><span>Address</span><strong>{user?.address || "Not set"}</strong></p>
      </CardContent></Card>
    </>
  );
}

export function PrescriptionsPage() {
  const token = useToken();
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const prescriptions = useQuery({ queryKey: ["prescriptions", token], queryFn: () => getPrescriptions(token) });
  const upload = useMutation({
    mutationFn: (file) => uploadPrescription(token, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["prescriptions"] }),
  });

  if (user?.role === "ADMIN") {
    return <Navigate to="/app/admin/orders" replace />;
  }

  return (
    <>
      <PageTitle icon={FileText} title="Prescriptions" text="Upload and review prescription status." />
      <Card><CardContent><Input type="file" onChange={(event) => event.target.files?.[0] && upload.mutate(event.target.files[0])} /><ErrorState error={upload.error || prescriptions.error} /></CardContent></Card>
      <ListCard items={asArray(prescriptions.data)} empty="No prescriptions uploaded yet" render={(item) => (
        <><strong>{item.fileName || item.originalFileName || `Prescription #${item.id}`}</strong><StatusBadge value={item.status} /><span>{formatDate(item.createdAt)}</span></>
      )} />
    </>
  );
}

export function OrdersPage() {
  const token = useToken();
  const orders = useQuery({ queryKey: ["orders", token], queryFn: () => getOrders(token) });
  return (
    <>
      <PageTitle icon={ClipboardList} title="Orders" text="Track current and past orders." />
      {orders.isLoading ? <LoadingState label="Loading orders" /> : null}
      <ErrorState error={orders.error} />
      <ListCard items={asArray(orders.data)} empty="No orders yet" render={(order) => (
        <><Link to={`/app/account/orders/${order.id}`}><strong>Order #{order.id}</strong></Link><StatusBadge value={order.status} /><span>{formatCurrency(order.totalAmount)}</span><span>{formatDate(order.createdAt)}</span></>
      )} />
    </>
  );
}

export function OrderDetailPage() {
  const { orderId } = useParams();
  const token = useToken();
  const queryClient = useQueryClient();
  const [paymentMethod, setPaymentMethod] = useState("UPI");
  const order = useQuery({ queryKey: ["order", token, orderId], queryFn: () => getOrder(token, orderId) });
  const cancel = useMutation({
    mutationFn: () => cancelOrder(token, orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["order"] });
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });
  const payment = useMutation({
    mutationFn: () => payForOrder(token, orderId, paymentMethod),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["order"] });
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });

  if (order.isLoading) return <LoadingState label="Loading order" />;
  if (order.error) return <ErrorState error={order.error} />;

  const canPay = order.data.status === "PAYMENT_PENDING" || order.data.status === "PRESCRIPTION_APPROVED";
  const rxMessage = {
    PRESCRIPTION_PENDING: "Your prescription is waiting for admin approval. Payment will be available after approval.",
    PRESCRIPTION_REJECTED: "Your prescription was rejected. This order cannot move to payment.",
    PRESCRIPTION_APPROVED: "Your prescription is approved. You can complete payment now.",
  }[order.data.status];

  return (
    <>
      <PageTitle title={`Order #${order.data.id}`} text={formatDate(order.data.createdAt)} />
      <div className="two-column">
        <Card><CardContent className="summary">
          <StatusBadge value={order.data.status} />
          <strong>{formatCurrency(order.data.totalAmount)}</strong>
          {rxMessage ? <p className="muted">{rxMessage}</p> : null}
          {order.data.prescriptionId ? <span className="muted">Prescription #{order.data.prescriptionId}</span> : null}
          {canPay ? (
            <>
              <Select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>
                {paymentMethods.map((method) => <option key={method}>{method}</option>)}
              </Select>
              <Button onClick={() => payment.mutate()} disabled={payment.isPending}>Pay now</Button>
            </>
          ) : null}
          <Button variant="outline" onClick={() => cancel.mutate()}>Cancel order</Button>
        </CardContent></Card>
        <Card>
          <CardHeader><CardTitle>Items</CardTitle></CardHeader>
          <CardContent className="table-list">
            {(order.data.items || []).map((item) => (
              <div className="table-row order-item-row" key={item.id || item.medicineId}>
                <strong>{item.medicineName || item.name}</strong>
                <span>Qty {item.quantity}</span>
                <span>{formatCurrency(item.lineTotal || item.totalPrice)}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
      <ErrorState error={cancel.error || payment.error} />
    </>
  );
}

export function AdminDashboardPage() {
  const token = useToken();
  const dashboard = useQuery({ queryKey: ["admin-dashboard", token], queryFn: () => getAdminDashboard(token) });
  const orders = useQuery({ queryKey: ["admin-orders", token], queryFn: () => getAdminOrders(token) });
  const users = useQuery({ queryKey: ["admin-users", token], queryFn: () => getUsers(token) });
  return (
    <>
      <PageTitle icon={BarChart3} title="Admin dashboard" text="Operational summary." />
      <ErrorState error={dashboard.error || orders.error || users.error} />
      <div className="stat-grid">
        <Stat label="Revenue" value={formatCurrency(dashboard.data?.totalRevenue || 0)} />
        <Stat label="Orders" value={asArray(orders.data).length} />
        <Stat label="Users" value={asArray(users.data).length} />
      </div>
    </>
  );
}

const medicineSchema = z.object({
  name: z.string().min(2, "Name is required."),
  description: z.string().optional(),
  categoryId: z.coerce.number().min(1, "Category is required."),
  manufacturer: z.string().optional(),
  price: z.coerce.number().min(1, "Price is required."),
  stock: z.coerce.number().min(0, "Stock cannot be negative."),
  requiresPrescription: z.boolean().optional(),
  imageUrl: z.string().optional(),
  dosageInfo: z.string().optional(),
  sideEffects: z.string().optional(),
});

export function AdminMedicinesPage() {
  const token = useToken();
  const queryClient = useQueryClient();
  const medicines = useQuery({ queryKey: ["admin-medicines", token], queryFn: () => getMedicines({ size: 100 }) });
  const categories = useQuery({ queryKey: ["categories"], queryFn: getCategories });
  const [editingId, setEditingId] = useState("");
  const form = useForm({
    resolver: zodResolver(medicineSchema),
    defaultValues: {
      name: "",
      description: "",
      categoryId: "",
      manufacturer: "",
      price: 0,
      stock: 0,
      requiresPrescription: false,
      imageUrl: "",
      dosageInfo: "",
      sideEffects: "",
    },
  });
  const save = useMutation({
    mutationFn: (values) => saveMedicine(token, editingId, values),
    onSuccess: () => {
      form.reset({
        name: "",
        description: "",
        categoryId: "",
        manufacturer: "",
        price: 0,
        stock: 0,
        requiresPrescription: false,
        imageUrl: "",
        dosageInfo: "",
        sideEffects: "",
      });
      setEditingId("");
      queryClient.invalidateQueries({ queryKey: ["admin-medicines"] });
    },
  });
  const remove = useMutation({ mutationFn: (id) => deleteMedicine(token, id), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-medicines"] }) });

  function resetMedicineForm() {
    form.reset({
      name: "",
      description: "",
      categoryId: "",
      manufacturer: "",
      price: 0,
      stock: 0,
      requiresPrescription: false,
      imageUrl: "",
      dosageInfo: "",
      sideEffects: "",
    });
    setEditingId("");
  }

  function edit(medicine) {
    setEditingId(medicine.id);
    form.reset({
      name: medicine.name || "",
      description: medicine.description || "",
      categoryId: medicine.category?.id || medicine.categoryId || "",
      manufacturer: medicine.manufacturer || "",
      price: medicine.price,
      stock: medicine.stock,
      requiresPrescription: Boolean(medicine.requiresPrescription),
      imageUrl: medicine.imageUrl || "",
      dosageInfo: medicine.dosageInfo || "",
      sideEffects: medicine.sideEffects || "",
    });
  }

  return (
    <>
      <PageTitle icon={Package} title="Medicines" text="Add or update catalog items." />
      <Card className="medicine-editor">
        <CardHeader className="editor-header">
          <div>
            <CardTitle>{editingId ? "Edit medicine" : "Add medicine"}</CardTitle>
            <p className="muted">{editingId ? "Update catalog, pricing, and clinical details." : "Create a complete catalog entry."}</p>
          </div>
          {editingId ? <Badge tone="warning">Editing #{editingId}</Badge> : null}
        </CardHeader>
        <CardContent>
          <form className="medicine-form" onSubmit={form.handleSubmit((values) => save.mutate(values))}>
            <section className="form-section">
              <h3>Basic details</h3>
              <div className="medicine-form-grid">
                <Field label="Name" error={form.formState.errors.name?.message}><Input {...form.register("name")} /></Field>
                <Field label="Category" error={form.formState.errors.categoryId?.message}>
                  <Select {...form.register("categoryId")}>
                    <option value="">Select category</option>
                    {asArray(categories.data).map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
                  </Select>
                </Field>
                <Field label="Manufacturer"><Input {...form.register("manufacturer")} /></Field>
                <Field label="Price" error={form.formState.errors.price?.message}><Input type="number" min="0" step="0.01" {...form.register("price")} /></Field>
                <Field label="Stock" error={form.formState.errors.stock?.message}><Input type="number" min="0" {...form.register("stock")} /></Field>
                <Field label="Image URL" className="span-2"><Input placeholder="/medicine-images/default.svg" {...form.register("imageUrl")} /></Field>
                <label className="check-row rx-toggle"><input type="checkbox" {...form.register("requiresPrescription")} /> Rx required</label>
              </div>
            </section>

            <section className="form-section">
              <h3>Clinical details</h3>
              <div className="medicine-form-grid details-grid">
                <Field label="Description"><Textarea {...form.register("description")} /></Field>
                <Field label="Dosage info"><Textarea {...form.register("dosageInfo")} /></Field>
                <Field label="Side effects"><Textarea {...form.register("sideEffects")} /></Field>
              </div>
            </section>

            <div className="form-actions">
              {editingId ? <Button type="button" variant="outline" onClick={resetMedicineForm}>Cancel</Button> : null}
              <Button disabled={save.isPending}><Plus size={16} /> {editingId ? "Update medicine" : "Add medicine"}</Button>
            </div>
          </form>
          <ErrorState error={categories.error || save.error || remove.error} />
        </CardContent>
      </Card>
      <ListCard items={asArray(medicines.data)} empty="No medicines found" render={(medicine) => (
        <><strong>{medicine.name}</strong><span>{formatCurrency(medicine.price)}</span><Badge tone={medicine.stock > 0 ? "success" : "danger"}>{medicine.stock} stock</Badge><Button variant="secondary" size="sm" onClick={() => edit(medicine)}>Edit</Button><Button variant="danger" size="sm" onClick={() => remove.mutate(medicine.id)}>Delete</Button></>
      )} />
    </>
  );
}

export function AdminOrdersPage() {
  const token = useToken();
  const queryClient = useQueryClient();
  const orders = useQuery({ queryKey: ["admin-orders", token], queryFn: () => getAdminOrders(token) });
  const update = useMutation({
    mutationFn: ({ id, status }) => updateAdminOrderStatus(token, id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-orders"] }),
  });
  const previewPrescription = useMutation({
    mutationFn: async ({ prescriptionId }) => getPrescriptionFile(token, prescriptionId),
    onSuccess: ({ blob, contentType }) => {
      const previewBlob = contentType ? blob.slice(0, blob.size, contentType) : blob;
      const previewUrl = URL.createObjectURL(previewBlob);
      window.open(previewUrl, "_blank", "noopener,noreferrer");
      window.setTimeout(() => URL.revokeObjectURL(previewUrl), 60_000);
    },
  });
  return (
    <>
      <PageTitle icon={ClipboardList} title="Admin orders" text="Update fulfillment status." />
      <ErrorState error={orders.error || update.error || previewPrescription.error} />
      <ListCard items={asArray(orders.data)} empty="No orders found" render={(order) => (
        <>
          <strong>#{order.id}</strong>
          <StatusBadge value={order.status} />
          <span>{formatCurrency(order.totalAmount)}</span>
          {order.prescriptionId ? (
            <Button
              variant="secondary"
              size="sm"
              disabled={previewPrescription.isPending}
              onClick={() => previewPrescription.mutate({ prescriptionId: order.prescriptionId })}
            >
              {previewPrescription.isPending ? "Opening..." : order.prescriptionFileName || `Prescription #${order.prescriptionId}`}
            </Button>
          ) : (
            <span className="muted">No prescription</span>
          )}
          {order.prescriptionStatus ? <StatusBadge value={order.prescriptionStatus} /> : null}
          {order.status === "PRESCRIPTION_PENDING" ? (
            <>
              <Button variant="secondary" size="sm" onClick={() => update.mutate({ id: order.id, status: "PRESCRIPTION_APPROVED" })}>Approve Rx</Button>
              <Button variant="danger" size="sm" onClick={() => update.mutate({ id: order.id, status: "PRESCRIPTION_REJECTED" })}>Reject Rx</Button>
            </>
          ) : null}
          <Select value={order.status} onChange={(event) => update.mutate({ id: order.id, status: event.target.value })}>{orderStatuses.map((status) => <option key={status}>{status}</option>)}</Select>
        </>
      )} />
    </>
  );
}

export function AdminUsersPage() {
  const token = useToken();
  const queryClient = useQueryClient();
  const users = useQuery({ queryKey: ["admin-users", token], queryFn: () => getUsers(token) });
  const update = useMutation({
    mutationFn: ({ id, role, active }) => updateUser(token, id, { role, active }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });
  return (
    <>
      <PageTitle icon={Users} title="Users" text="Manage roles and access." />
      <ErrorState error={users.error || update.error} />
      <ListCard items={asArray(users.data)} empty="No users found" render={(user) => (
        <><strong>{user.name || user.email}</strong><span>{user.email}</span><Select value={user.role} onChange={(event) => update.mutate({ id: user.id, role: event.target.value, active: user.active ?? user.isActive })}>{["CUSTOMER", "ADMIN", "PHARMACIST", "DELIVERY_AGENT"].map((role) => <option key={role}>{role}</option>)}</Select><Button variant="secondary" size="sm" onClick={() => update.mutate({ id: user.id, role: user.role, active: !(user.active ?? user.isActive) })}>{(user.active ?? user.isActive) ? "Deactivate" : "Activate"}</Button></>
      )} />
    </>
  );
}

export function AdminReportsPage() {
  const token = useToken();
  const sales = useQuery({ queryKey: ["sales-report", token], queryFn: () => getSalesReport(token) });
  const inventory = useQuery({ queryKey: ["inventory-report", token], queryFn: () => getInventoryReport(token) });
  const lowStock = asArray(inventory.data?.lowStockItems || inventory.data);

  return (
    <>
      <PageTitle icon={BarChart3} title="Reports" text="Sales and inventory overview." />
      <ErrorState error={sales.error || inventory.error} />
      <div className="stat-grid">
        <Stat label="Sales revenue" value={formatCurrency(sales.data?.totalRevenue || 0)} />
        <Stat label="Orders sold" value={sales.data?.totalOrders || 0} />
        <Stat label="Low stock items" value={lowStock.length} />
      </div>
      <ListCard items={lowStock} empty="No low stock items" render={(item) => <><strong>{item.name || item.medicineName}</strong><Badge tone="warning">{item.stock} stock</Badge></>} />
    </>
  );
}

export function NotFoundPage() {
  return (
    <main className="auth-page">
      <Card className="auth-card">
        <CardContent className="empty">
          <h1>Page not found</h1>
          <Button asChild><Link to="/">Go home</Link></Button>
        </CardContent>
      </Card>
    </main>
  );
}
