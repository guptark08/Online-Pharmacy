import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import FormField from "../components/forms/FormField";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { signupUser } from "../store/slices/authSlice";

export default function SignupPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const authStatus = useAppSelector((state) => state.auth.status);
  const error = useAppSelector((state) => state.auth.error);
  const [formState, setFormState] = useState({
    name: "",
    email: "",
    mobile: "",
    address: "",
    password: "",
  });

  useDocumentTitle("Signup");

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormState((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await dispatch(signupUser(formState)).unwrap();
    navigate("/app");
  };

  return (
    <div className="auth-shell">
      <Card eyebrow="Registration" title="Create your PulseMeds account" accent="mint">
        <ErrorAlert message={error} />

        <form className="form-stack" onSubmit={handleSubmit}>
          <FormField label="Full Name">
            <input name="name" value={formState.name} onChange={handleChange} required />
          </FormField>

          <FormField label="Email">
            <input type="email" name="email" value={formState.email} onChange={handleChange} required />
          </FormField>

          <FormField label="Mobile">
            <input name="mobile" value={formState.mobile} onChange={handleChange} required />
          </FormField>

          <FormField label="Address">
            <textarea name="address" value={formState.address} onChange={handleChange} rows="3" required />
          </FormField>

          <FormField label="Password" hint="Use a strong password to secure your account.">
            <input type="password" name="password" value={formState.password} onChange={handleChange} required />
          </FormField>

          <Button type="submit" block disabled={authStatus === "loading"}>
            {authStatus === "loading" ? "Creating account..." : "Create Account"}
          </Button>
        </form>

        <p className="helper-text">
          Already registered? <Link to="/login">Sign in instead</Link>.
        </p>
      </Card>
    </div>
  );
}
