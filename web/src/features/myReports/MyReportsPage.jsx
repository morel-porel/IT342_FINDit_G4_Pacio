import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import api from "../../shared/api/api";

const STATUS_META = {
    "OPEN":     { label: "Open",     cls: "status-open" },
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
    "RESOLVED": { label: "Resolved", cls: "status-resolved" },
};

const FILTER_CHIPS = [
    { val: "ALL",      label: "All" },
    { val: "LOST",     label: "Lost" },
    { val: "FOUND",    label: "Found" },
    { val: "OPEN",     label: "Open" },
    { val: "RESOLVED", label: "Resolved" },
];

function MyReportsPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filter, setFilter] = useState("ALL");
    const [confirmItem, setConfirmItem] = useState(null); // item pending resolve confirmation

    const initials = user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?";

    useEffect(() => {
        api.get("/items/my")
            .then(res => setItems(res.data))
            .catch(() => setError("Failed to load your reports."))
            .finally(() => setLoading(false));
    }, []);

    const filtered = items.filter(item => {
        if (filter === "ALL")      return true;
        if (filter === "LOST")     return item.type === "LOST";
        if (filter === "FOUND")    return item.type === "FOUND";
        if (filter === "OPEN")     return item.status === "OPEN";
        if (filter === "RESOLVED") return item.status === "RESOLVED";
        return true;
    });

    const handleMarkResolved = (item) => {
        setConfirmItem(item);
    };

    const confirmResolve = () => {
        setItems(prev => prev.map(i =>
            i.id === confirmItem.id ? { ...i, status: "RESOLVED" } : i
        ));
        setConfirmItem(null);
    };

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo" onClick={() => navigate("/dashboard")}>FINDit</div>
                <div className="topbar-actions">
                    <button className="topnav-btn topnav-btn-active">My Reports</button>
                    <button className="topnav-btn" onClick={() => navigate("/my-claims")}>My Claims</button>
                    <div className="avatar" onClick={() => navigate("/profile")}>{initials}</div>
                </div>
            </nav>

            <div className="my-reports-page">
                {/* ── Title + filter chips ── */}
                <div className="my-reports-header">
                    <h2 className="my-reports-title">My Reports</h2>
                    <div className="chip-bar">
                        {FILTER_CHIPS.map(({ val, label }) => (
                            <button
                                key={val}
                                className={`chip ${filter === val ? "chip-active" : ""}`}
                                onClick={() => setFilter(val)}
                            >
                                {label}
                            </button>
                        ))}
                    </div>
                </div>

                {loading && <p className="loading-text">Loading...</p>}
                {error   && <p className="error-text">{error}</p>}

                {!loading && !error && (
                    <div className="reports-table-wrapper">
                        <table className="reports-table">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Type</th>
                                    <th>Category</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.length === 0 ? (
                                    <tr>
                                        <td colSpan={6} className="table-empty">
                                            No reports found.{" "}
                                            <button className="link-btn" onClick={() => navigate("/report")}>
                                                Report your first item
                                            </button>
                                        </td>
                                    </tr>
                                ) : (
                                    filtered.map(item => {
                                        const isLost = item.type === "LOST";
                                        const sm = STATUS_META[item.status] || { label: item.status, cls: "status-open" };
                                        const date = new Date(item.dateLostFound).toLocaleDateString("en-US", {
                                            month: "short", day: "numeric", year: "numeric"
                                        });
                                        const canResolve = item.status === "OPEN" || item.status === "APPROVED";

                                        return (
                                            <tr key={item.id}>
                                                <td>
                                                    <div className="table-item-name"
                                                        onClick={() => navigate(`/items/${item.id}`)}>
                                                        {item.name}
                                                    </div>
                                                    <div className="table-item-loc">📍 {item.location}</div>
                                                </td>
                                                <td>
                                                    <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                                                        {isLost ? "Lost" : "Found"}
                                                    </span>
                                                </td>
                                                <td>
                                                    <span className="category-tag">{item.category}</span>
                                                </td>
                                                <td className="table-date">{date}</td>
                                                <td>
                                                    <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                                                </td>
                                                <td>
                                                    {item.status !== "RESOLVED" ? (
                                                        <div className="table-actions">
                                                            <button className="action-btn action-edit"
                                                                onClick={() => navigate(`/items/${item.id}`)}>
                                                                Edit
                                                            </button>
                                                            {canResolve && (
                                                                <button className="action-btn action-resolve"
                                                                    onClick={() => handleMarkResolved(item)}>
                                                                    ✓ Mark Resolved
                                                                </button>
                                                            )}
                                                        </div>
                                                    ) : (
                                                        <span className="table-dash">—</span>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })
                                )}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* ── Inline resolve confirmation ── */}
                {confirmItem && (
                    <div className="confirm-banner">
                        <div className="confirm-banner-content">
                            <strong>Mark as Resolved?</strong>
                            <p>
                                Marking <em>{confirmItem.name}</em> as resolved means you have recovered it
                                or no longer need help finding it. This cannot be undone.
                            </p>
                            <div className="confirm-banner-actions">
                                <button className="action-btn action-resolve"
                                    onClick={confirmResolve}>
                                    Yes, Mark Resolved
                                </button>
                                <button className="action-btn action-cancel"
                                    onClick={() => setConfirmItem(null)}>
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default MyReportsPage;