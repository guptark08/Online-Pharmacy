import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import FormField from "../components/forms/FormField";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { fetchCart, selectCartRequiresPrescription } from "../store/slices/cartSlice";
import {
  fetchAddresses,
  fetchPrescriptions,
  saveAddress,
  startCheckout,
  uploadPrescription,
} from "../store/slices/orderSlice";
import { DELIVERY_SLOTS } from "../utils/constants";
import { formatCurrency, humanizeEnum } from "../utils/format";

export default function CheckoutPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart.cart);
  const cartStatus = useAppSelector((state) => state.cart.status);
  const requiresPrescription = useAppSelector(selectCartRequiresPrescription);
  const {
    addresses,
    prescriptions,
    addressStatus,
    prescriptionStatus,
    checkoutStatus,
    error,
  } = useAppSelector((state) => state.orders);

  const availablePrescriptions = useMemo(
    () => prescriptions.filter((prescription) => prescription.status !== "REJECTED"),
    [prescriptions],
  );

  const [addressForm, setAddressForm] = useState({
    fullName: "",
    mobile: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    pincode: "",
    isDefault: true,
  });
  const [checkoutForm, setCheckoutForm] = useState({
    addressId: "",
    prescriptionId: "",
    deliverySlot: DELIVERY_SLOTS[0],
  });
  const [selectedPrescriptionFile, setSelectedPrescriptionFile] = useState(null);
  const [prescriptionUploadStatus, setPrescriptionUploadStatus] = useState("idle");
  const [prescriptionUploadError, setPrescriptionUploadError] = useState("");

  useDocumentTitle("Checkout");

  useEffect(() => {
    dispatch(fetchCart());
    dispatch(fetchAddresses());
  }, [dispatch]);

  useEffect(() => {
    if (requiresPrescription) {
      dispatch(fetchPrescriptions());
    }
  }, [dispatch, requiresPrescription]);

  useEffect(() => {
    if (!checkoutForm.addressId && addresses.length > 0) {
      const defaultAddress = addresses.find((item) => item.default || item.isDefault) || addresses[0];
      setCheckoutForm((current) => ({
        ...current,
        addressId: String(defaultAddress.id),
      }));
    }
  }, [addresses, checkoutForm.addressId]);

  useEffect(() => {
    if (requiresPrescription && !checkoutForm.prescriptionId && availablePrescriptions.length > 0) {
      setCheckoutForm((current) => ({
        ...current,
        prescriptionId: String(availablePrescriptions[0].id),
      }));
    }
  }, [availablePrescriptions, checkoutForm.prescriptionId, requiresPrescription]);

  useEffect(() => {
    if (requiresPrescription) {
      return;
    }

    setCheckoutForm((current) => ({
      ...current,
      prescriptionId: "",
    }));
    setSelectedPrescriptionFile(null);
    setPrescriptionUploadStatus("idle");
    setPrescriptionUploadError("");
  }, [requiresPrescription]);

  const handleAddressInput = (event) => {
    const { name, value, type, checked } = event.target;
    setAddressForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleCheckoutInput = (event) => {
    const { name, value } = event.target;
    setCheckoutForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handlePrescriptionFileChange = (event) => {
    const nextFile = event.target.files?.[0] || null;
    setSelectedPrescriptionFile(nextFile);
    setPrescriptionUploadStatus("idle");
    setPrescriptionUploadError("");
  };

  const handleUploadPrescription = async () => {
    if (!selectedPrescriptionFile) {
      setPrescriptionUploadError("Please choose a file first.");
      return;
    }

    setPrescriptionUploadStatus("loading");
    setPrescriptionUploadError("");

    try {
      const uploaded = await dispatch(uploadPrescription(selectedPrescriptionFile)).unwrap();
      setCheckoutForm((current) => ({
        ...current,
        prescriptionId: String(uploaded.id),
      }));
      setSelectedPrescriptionFile(null);
      setPrescriptionUploadStatus("succeeded");
    } catch (uploadError) {
      setPrescriptionUploadStatus("failed");
      setPrescriptionUploadError(
        uploadError?.message || uploadError || "Unable to upload prescription.",
      );
    }
  };

  const handleSaveAddress = async (event) => {
    event.preventDefault();
    const savedAddress = await dispatch(saveAddress(addressForm)).unwrap();
    setCheckoutForm((current) => ({
      ...current,
      addressId: String(savedAddress.id),
    }));
    setAddressForm({
      fullName: "",
      mobile: "",
      addressLine1: "",
      addressLine2: "",
      city: "",
      state: "",
      pincode: "",
      isDefault: true,
    });
  };

  const handleCheckout = async (event) => {
    event.preventDefault();

    let resolvedPrescriptionId = checkoutForm.prescriptionId
      ? Number(checkoutForm.prescriptionId)
      : null;

    if (!resolvedPrescriptionId && selectedPrescriptionFile) {
      setPrescriptionUploadStatus("loading");
      setPrescriptionUploadError("");

      try {
        const uploaded = await dispatch(uploadPrescription(selectedPrescriptionFile)).unwrap();
        resolvedPrescriptionId = uploaded.id;
        setCheckoutForm((current) => ({
          ...current,
          prescriptionId: String(uploaded.id),
        }));
        setSelectedPrescriptionFile(null);
        setPrescriptionUploadStatus("succeeded");
      } catch (uploadError) {
        setPrescriptionUploadStatus("failed");
        setPrescriptionUploadError(
          uploadError?.message || uploadError || "Unable to upload prescription.",
        );
        return;
      }
    }

    const order = await dispatch(
      startCheckout({
        addressId: Number(checkoutForm.addressId),
        prescriptionId: resolvedPrescriptionId,
        deliverySlot: checkoutForm.deliverySlot,
      }),
    ).unwrap();

    dispatch(fetchCart());
    navigate(`/app/account/orders/${order.id}`);
  };

  if (cartStatus === "loading" && cart.items.length === 0) {
    return <LoadingState title="Preparing checkout" description="Loading cart, address, and prescription details." />;
  }

  if (cart.items.length === 0) {
    return (
      <EmptyState
        title="No items ready for checkout"
        description="Add medicines to your cart before placing an order."
      >
        <Button onClick={() => navigate("/app/catalog")}>Back to Catalog</Button>
      </EmptyState>
    );
  }

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Checkout"
        title="Confirm delivery details and place your order"
        description="Select address, attach prescription if required, and create the order."
      />

      <ErrorAlert message={error} />

      <div className="grid grid--checkout">
        <div className="stack">
          <Card title="Saved Addresses" accent="glass">
            {addressStatus === "loading" && addresses.length === 0 ? (
              <LoadingState title="Loading addresses" description="Fetching saved delivery locations." />
            ) : addresses.length === 0 ? (
              <EmptyState title="No addresses saved" description="Add an address below to continue." />
            ) : (
              <div className="stack">
                {addresses.map((address) => (
                  <label key={address.id} className="selectable-card">
                    <input
                      type="radio"
                      name="addressId"
                      value={address.id}
                      checked={checkoutForm.addressId === String(address.id)}
                      onChange={handleCheckoutInput}
                    />
                    <div>
                      <strong>{address.fullName}</strong>
                      <p>
                        {address.addressLine1}, {address.city}, {address.state} {address.pincode}
                      </p>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </Card>

          <Card title="Add New Address" accent="mint">
            <form className="form-stack" onSubmit={handleSaveAddress}>
              <FormField label="Full Name">
                <input name="fullName" value={addressForm.fullName} onChange={handleAddressInput} required />
              </FormField>
              <FormField label="Mobile">
                <input name="mobile" value={addressForm.mobile} onChange={handleAddressInput} required />
              </FormField>
              <FormField label="Address Line 1">
                <input name="addressLine1" value={addressForm.addressLine1} onChange={handleAddressInput} required />
              </FormField>
              <FormField label="Address Line 2">
                <input name="addressLine2" value={addressForm.addressLine2} onChange={handleAddressInput} />
              </FormField>
              <div className="grid grid--three">
                <FormField label="City">
                  <input name="city" value={addressForm.city} onChange={handleAddressInput} required />
                </FormField>
                <FormField label="State">
                  <input name="state" value={addressForm.state} onChange={handleAddressInput} required />
                </FormField>
                <FormField label="Pincode">
                  <input name="pincode" value={addressForm.pincode} onChange={handleAddressInput} required />
                </FormField>
              </div>
              <label className="checkbox-field">
                <input
                  type="checkbox"
                  name="isDefault"
                  checked={addressForm.isDefault}
                  onChange={handleAddressInput}
                />
                <span>Save as default address</span>
              </label>
              <Button type="submit" disabled={addressStatus === "loading"}>
                {addressStatus === "loading" ? "Saving..." : "Save Address"}
              </Button>
            </form>
          </Card>

          <Card title="Delivery & Order Confirmation" accent="glass">
            <form className="form-stack" onSubmit={handleCheckout}>
              <FormField label="Delivery Slot">
                <select
                  name="deliverySlot"
                  value={checkoutForm.deliverySlot}
                  onChange={handleCheckoutInput}
                >
                  {DELIVERY_SLOTS.map((slot) => (
                    <option key={slot} value={slot}>
                      {slot}
                    </option>
                  ))}
                </select>
              </FormField>

              {requiresPrescription ? (
                <>
                  <FormField
                    label="Prescription"
                    hint="Choose an existing file or upload a new prescription below."
                  >
                    <select
                      name="prescriptionId"
                      value={checkoutForm.prescriptionId}
                      onChange={handleCheckoutInput}
                      required
                    >
                      <option value="">Select prescription</option>
                      {availablePrescriptions.map((prescription) => (
                        <option key={prescription.id} value={prescription.id}>
                          #{prescription.id} - {humanizeEnum(prescription.status)}
                        </option>
                      ))}
                    </select>
                  </FormField>

                  <FormField
                    label="Upload Prescription"
                    hint="Upload JPG, PNG, or PDF."
                    error={prescriptionUploadError}
                  >
                    <input
                      type="file"
                      accept=".pdf,image/png,image/jpeg"
                      onChange={handlePrescriptionFileChange}
                    />
                  </FormField>

                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleUploadPrescription}
                    disabled={prescriptionUploadStatus === "loading" || !selectedPrescriptionFile}
                  >
                    {prescriptionUploadStatus === "loading" ? "Uploading..." : "Upload & Attach Prescription"}
                  </Button>

                  {prescriptionStatus === "loading" ? (
                    <LoadingState title="Loading prescriptions" description="Checking existing uploads for checkout." />
                  ) : null}

                  {availablePrescriptions.length === 0 ? (
                    <EmptyState
                      title="Prescription required"
                      description="Upload a valid prescription to continue checkout."
                    />
                  ) : null}
                </>
              ) : (
                <p className="helper-text">Prescription is not required for the current cart items.</p>
              )}

              <Button
                type="submit"
                disabled={
                  checkoutStatus === "loading" ||
                  !checkoutForm.addressId ||
                  (requiresPrescription && !checkoutForm.prescriptionId)
                }
              >
                {checkoutStatus === "loading" ? "Creating order..." : "Place Order"}
              </Button>
            </form>
          </Card>
        </div>

        <Card title="Cart Summary" accent="sunrise" className="sticky-card">
          <div className="stack">
            {cart.items.map((item) => (
              <div key={item.id} className="summary-row">
                <span>
                  {item.medicineName} x {item.quantity}
                </span>
                <strong>{formatCurrency(item.subtotal)}</strong>
              </div>
            ))}
          </div>
          <hr className="divider" />
          <div className="summary-row">
            <span>Total</span>
            <strong>{formatCurrency(cart.totalAmount)}</strong>
          </div>
          {requiresPrescription ? <StatusBadge status="PRESCRIPTION_PENDING" /> : null}
        </Card>
      </div>
    </div>
  );
}
