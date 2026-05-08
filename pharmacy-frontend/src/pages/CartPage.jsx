import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import QuantityControl from "../components/cart/QuantityControl";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import EmptyState from "../components/common/EmptyState";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import {
  clearCart,
  fetchCart,
  updateCartItem,
  removeCartItem,
} from "../store/slices/cartSlice";
import { formatCurrency } from "../utils/format";

export default function CartPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const cart = useAppSelector((state) => state.cart.cart);
  const status = useAppSelector((state) => state.cart.status);
  const error = useAppSelector((state) => state.cart.error);

  useDocumentTitle("Cart");

  useEffect(() => {
    dispatch(fetchCart());
  }, [dispatch]);

  const handleQuantityChange = (itemId, quantity) => {
    if (quantity < 1) {
      dispatch(removeCartItem(itemId));
      return;
    }

    dispatch(updateCartItem({ itemId, quantity }));
  };

  const handleClearCart = () => {
    dispatch(clearCart());
  };

  if (status === "loading" && cart.items.length === 0) {
    return <LoadingState title="Loading your cart" description="Reviewing saved items and totals." />;
  }

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Cart"
        title="Review your selected medicines"
        description="Update quantities, remove items, and proceed to checkout."
      />

      <ErrorAlert message={error} />

      {cart.items.length === 0 ? (
        <EmptyState
          title="Your cart is empty"
          description="Search the catalog and add medicines to continue."
        >
          <Button onClick={() => navigate("/app/catalog")}>Go to Catalog</Button>
        </EmptyState>
      ) : (
        <div className="grid grid--cart">
          <div className="stack">
            {cart.items.map((item) => (
              <Card key={item.id} accent="glass" className="cart-item">
                <div className="cart-item__main">
                  <div>
                    <h3>{item.medicineName}</h3>
                    <p>{formatCurrency(item.price)} each</p>
                  </div>
                  {item.requiresPrescription ? <StatusBadge status="PRESCRIPTION_PENDING" /> : null}
                </div>

                <div className="cart-item__footer">
                  <QuantityControl
                    quantity={item.quantity}
                    onDecrease={() => handleQuantityChange(item.id, item.quantity - 1)}
                    onIncrease={() => handleQuantityChange(item.id, item.quantity + 1)}
                  />

                  <div className="cart-item__summary">
                    <strong>{formatCurrency(item.subtotal)}</strong>
                    <button type="button" className="text-button" onClick={() => dispatch(removeCartItem(item.id))}>
                      Remove
                    </button>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          <Card title="Order Summary" accent="sunrise" className="sticky-card">
            <div className="summary-row">
              <span>Total items</span>
              <strong>{cart.totalItems}</strong>
            </div>
            <div className="summary-row">
              <span>Total amount</span>
              <strong>{formatCurrency(cart.totalAmount)}</strong>
            </div>
            <div className="stack">
              <Button block onClick={() => navigate("/app/account/checkout")}>
                Proceed to Checkout
              </Button>
              <Button block variant="ghost" onClick={handleClearCart}>
                Clear Cart
              </Button>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
}
