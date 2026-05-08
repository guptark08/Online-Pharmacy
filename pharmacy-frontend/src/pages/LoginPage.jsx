import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import FormField from "../components/forms/FormField";
import { useAuth } from "../hooks/useAuth";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { loginUser } from "../store/slices/authSlice";

export default function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isAdmin } = useAuth();
  const authStatus = useAppSelector((state) => state.auth.status);
  const error = useAppSelector((state) => state.auth.error);
  const [formState, setFormState] = useState({
    email: "",
    password: "",
  });

  useDocumentTitle("Login");

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    navigate(isAdmin ? "/app/admin/prescriptions" : "/app", { replace: true });
  }, [isAuthenticated, isAdmin, navigate]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormState((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const session = await dispatch(loginUser(formState)).unwrap();
    const intendedPath = location.state?.from?.pathname;

    if (session.user?.role === "ADMIN") {
      navigate("/app/admin/prescriptions");
      return;
    }

    navigate(intendedPath || "/app");
  };

  return (
    <div className="auth-shell auth-shell--login">
      <Card eyebrow="Sign In" title="Welcome back to PulseMeds" accent="glass" className="auth-card--login">
        <ErrorAlert message={error} />

        <form className="form-stack" onSubmit={handleSubmit} autoComplete="off">
          <FormField label="Email">
            <input
              type="email"
              name="email"
              value={formState.email}
              onChange={handleChange}
              placeholder="you@example.com"
              autoComplete="off"
              spellCheck="false"
              required
            />
          </FormField>

          <FormField label="Password">
            <input
              type="password"
              name="password"
              value={formState.password}
              onChange={handleChange}
              placeholder="Enter your password"
              autoComplete="off"
              required
            />
          </FormField>

          <Button type="submit" block disabled={authStatus === "loading"}>
            {authStatus === "loading" ? "Signing in..." : "Login"}
          </Button>
        </form>

        <p className="helper-text">
          Need a new account? <Link to="/signup">Create one here</Link>.
        </p>
      </Card>
    </div>
  );
}
