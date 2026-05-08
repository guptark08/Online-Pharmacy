import Card from "../components/common/Card";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import { useAuth } from "../hooks/useAuth";
import { useDocumentTitle } from "../hooks/useDocumentTitle";

export default function ProfilePage() {
  const { user } = useAuth();

  useDocumentTitle("Profile");

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="Profile"
        title="Customer account details"
        description="Review your contact details used for medicine delivery and order communication."
      />

      <div className="grid grid--two">
        <Card title="Identity" accent="glass">
          <div className="stack">
            <p><strong>Name:</strong> {user?.name || "Unknown user"}</p>
            <p><strong>Email:</strong> {user?.email || "Not available"}</p>
            <p><strong>Mobile:</strong> {user?.mobile || "Not available"}</p>
            <p><strong>Address:</strong> {user?.address || "No saved address yet"}</p>
          </div>
        </Card>

        <Card title="Access" accent="mint">
          <div className="stack">
            <p><strong>Role:</strong> {user?.role || "CUSTOMER"}</p>
            <StatusBadge status={user?.isActive === false ? "INACTIVE" : "ACTIVE"} />
            <p>Contact support if any profile information needs correction.</p>
          </div>
        </Card>
      </div>
    </div>
  );
}
