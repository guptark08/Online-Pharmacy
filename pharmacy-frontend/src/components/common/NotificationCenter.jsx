import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { formatDate } from "../../utils/format";

function toneToClass(tone) {
  switch (tone) {
    case "success":
      return "notification-item--success";
    case "warning":
      return "notification-item--warning";
    case "danger":
      return "notification-item--danger";
    default:
      return "notification-item--info";
  }
}

export default function NotificationCenter({
  title = "Notifications",
  notifications = [],
  unreadCount = 0,
  onMarkAllRead,
  orderLinkBase = "/app/account/orders",
  linkToOrderDetail = true,
}) {
  const [isOpen, setIsOpen] = useState(false);

  const topNotifications = useMemo(
    () => notifications.slice(0, 8),
    [notifications],
  );

  const handleToggle = () => {
    const nextIsOpen = !isOpen;
    setIsOpen(nextIsOpen);
    if (nextIsOpen && unreadCount > 0 && typeof onMarkAllRead === "function") {
      onMarkAllRead();
    }
  };

  return (
    <div className="notification-center">
      <button
        type="button"
        className="notification-center__trigger"
        onClick={handleToggle}
      >
        <span>{title}</span>
        {unreadCount > 0 ? (
          <span className="notification-center__count">{unreadCount}</span>
        ) : null}
      </button>

      {isOpen ? (
        <section className="notification-center__panel">
          <header className="notification-center__header">
            <strong>{title}</strong>
            <button type="button" className="text-button" onClick={() => setIsOpen(false)}>
              Close
            </button>
          </header>

          {topNotifications.length === 0 ? (
            <p className="helper-text">No new updates yet.</p>
          ) : (
            <div className="notification-center__list">
              {topNotifications.map((notification) => (
                <article
                  key={notification.id}
                  className={["notification-item", toneToClass(notification.tone)].join(" ")}
                >
                  <strong>{notification.title}</strong>
                  <p>{notification.message}</p>
                  <div className="notification-item__footer">
                    <span>{formatDate(notification.createdAt)}</span>
                    {notification.orderId ? (
                      <Link
                        to={linkToOrderDetail ? `${orderLinkBase}/${notification.orderId}` : orderLinkBase}
                      >
                        {linkToOrderDetail ? "Track order" : "Open orders"}
                      </Link>
                    ) : null}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      ) : null}
    </div>
  );
}
