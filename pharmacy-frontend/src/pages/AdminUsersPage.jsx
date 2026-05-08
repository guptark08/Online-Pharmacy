import { useEffect, useState } from "react";
import Button from "../components/common/Button";
import Card from "../components/common/Card";
import ErrorAlert from "../components/common/ErrorAlert";
import LoadingState from "../components/common/LoadingState";
import SectionHeading from "../components/common/SectionHeading";
import StatusBadge from "../components/common/StatusBadge";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { useDocumentTitle } from "../hooks/useDocumentTitle";
import { fetchUsers, updateUser } from "../store/slices/adminSlice";
import { ROLE_OPTIONS } from "../utils/constants";
import { formatDate, humanizeEnum } from "../utils/format";

export default function AdminUsersPage() {
  const dispatch = useAppDispatch();
  const users = useAppSelector((state) => state.admin.users);
  const status = useAppSelector((state) => state.admin.usersStatus);
  const error = useAppSelector((state) => state.admin.error);
  const [draftUsers, setDraftUsers] = useState({});

  useDocumentTitle("Admin Users");

  useEffect(() => {
    dispatch(fetchUsers());
  }, [dispatch]);

  useEffect(() => {
    const mappedUsers = {};
    users.forEach((user) => {
      mappedUsers[user.id] = {
        role: user.role,
        active: user.isActive,
      };
    });
    setDraftUsers(mappedUsers);
  }, [users]);

  const handleDraftChange = (userId, field, value) => {
    setDraftUsers((current) => ({
      ...current,
      [userId]: {
        ...current[userId],
        [field]: value,
      },
    }));
  };

  const handleSave = (userId) => {
    dispatch(updateUser({ userId, ...draftUsers[userId] }));
  };

  return (
    <div className="page-stack">
      <SectionHeading
        eyebrow="User Management"
        title="Manage account roles and access"
        description="Update role permissions and activate or deactivate accounts."
      />

      <ErrorAlert message={error} />

      {status === "loading" && users.length === 0 ? (
        <LoadingState title="Loading users" description="Fetching accounts from the auth service." />
      ) : (
        <div className="stack">
          {users.map((user) => (
            <Card key={user.id} accent="glass" title={user.name}>
              <div className="summary-row summary-row--stretch">
                <div>
                  <p><strong>Email:</strong> {user.email}</p>
                  <p><strong>Mobile:</strong> {user.mobile || "Not available"}</p>
                  <p><strong>Address:</strong> {user.address || "Not available"}</p>
                  <p><strong>Created:</strong> {formatDate(user.createdAt)}</p>
                </div>

                <div className="summary-row__stack">
                  <StatusBadge status={draftUsers[user.id]?.active ? "ACTIVE" : "INACTIVE"} />
                  <select
                    value={draftUsers[user.id]?.role || user.role}
                    onChange={(event) => handleDraftChange(user.id, "role", event.target.value)}
                  >
                    {ROLE_OPTIONS.map((role) => (
                      <option key={role} value={role}>
                        {humanizeEnum(role)}
                      </option>
                    ))}
                  </select>
                  <label className="checkbox-field">
                    <input
                      type="checkbox"
                      checked={draftUsers[user.id]?.active ?? user.isActive}
                      onChange={(event) =>
                        handleDraftChange(user.id, "active", event.target.checked)
                      }
                    />
                    <span>Account active</span>
                  </label>
                  <Button onClick={() => handleSave(user.id)}>Save User</Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
