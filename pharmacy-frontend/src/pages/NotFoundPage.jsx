import { Link } from "react-router-dom";
import Card from "../components/common/Card";
import { useDocumentTitle } from "../hooks/useDocumentTitle";

export default function NotFoundPage() {
  useDocumentTitle("Not Found");

  return (
    <div className="auth-shell">
      <Card title="Page not found" accent="glass">
        <p>The route you opened does not exist in this React Router setup.</p>
        <Link className="text-button" to="/app">
          Return to Home
        </Link>
      </Card>
    </div>
  );
}
