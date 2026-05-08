import { Link } from "react-router-dom";
import Button from "../components/common/Button";
import { useDocumentTitle } from "../hooks/useDocumentTitle";

export default function LandingPage() {
  useDocumentTitle("PulseMeds");

  return (
    <main className="landing-shell">
      <section className="landing-hero">
        <div className="landing-hero__brand">
          <img src="/images/branding/pulsemeds-logo.svg" alt="PulseMeds" />
          <div>
            <strong>PulseMeds</strong>
            <p>Your trusted online pharmacy partner.</p>
          </div>
        </div>

        <h1>Medicine delivery made easy, safe, and fast.</h1>
        <p>
          Search medicines, place orders, upload prescription only when needed, and track delivery
          updates from one dashboard.
        </p>

        <div className="landing-hero__actions">
          <Link to="/login">
            <Button>Sign In</Button>
          </Link>
          <Link to="/signup">
            <Button variant="ghost">Create Account</Button>
          </Link>
        </div>
      </section>
    </main>
  );
}
