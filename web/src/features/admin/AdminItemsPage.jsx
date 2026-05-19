import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../shared/api/api";
import AdminLayout from "./AdminLayout";

// ADMIN-02 — Item Management

const STATUS_META = {
    "OPEN":     { label: "Open",     cls: "status-open" },
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
    "RESOLVED": { label: "Resolved", cls: "status-resolved" },
};

function AdminItemsPage() {
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [typeFilter, setTypeFilter] = useState("ALL");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [deleteTarget, setDeleteTarget] = useState(null);
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        api.get("/items")
            .then(res => setItems(res.data))
            .catch(() => setError("Failed to load items."))
            .finally(() => setLoading(false));
    }, []);

    const filtered = items.filter(item => {
        const matchesType   = typeFilter === "ALL" || item.type === typeFilter;
        const matchesStatus = statusFilter === "ALL" || item.status === statusFilter;
        return matchesType && matchesStatus;
    });

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setDeleting(true);
        try {
            await api.delete(`/items/${deleteTarget.id}`);
            setItems(prev => prev.filter(i => i.id !== deleteTarget.id));
            setDeleteTarget(null);
        } catch {
            alert("Failed to delete item. Please try again.");
        } finally {
            setDeleting(false);
        }
    };

    return (
        <AdminLayout>
            <div className="admin-content">
                <div className="admin-page-header">
                    <h1 className="admin-page-title">Item Management</h1>
                    <div className="admin-filters">
                        <select
                            className="filter-select"
                            value={typeFilter}
                            onChange={e => setTypeFilter(e.target.value)}
                        >
                            <option value="ALL">All Types</option>
                            <option value="LOST">Lost</option>
                            <option value="FOUND">Found</option>
                        </select>
                        <select
                            className="filter-select"
                            value={statusFilter}
                            onChange={e => setStatusFilter(e.target.value)}
                        >
                            <option value="ALL">All Status</option>
                            <option value="OPEN">Open</option>
                            <option value="PENDING">Pending</option>
                            <option value="APPROVED">Approved</option>
                            <option value="REJECTED">Rejected</option>
                            <option value="RESOLVED">Resolved</option>
                        </select>
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
                                    <th>Reporter</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.length === 0 ? (
                                    <tr>
                                        <td colSpan={7} className="table-empty">No items found.</td>
                                    </tr>
                                ) : filtered.map(item => {
                                    const isLost = item.type === "LOST";
                                    const sm = STATUS_META[item.status] || { label: item.status, cls: "status-open" };
                                    const dateStr = new Date(item.dateLostFound).toLocaleDateString("en-US", {
                                        month: "short", day: "numeric", year: "numeric"
                                    });
                                    const reporterFirst = item.reporter?.fullName?.split(" ")[0] || "—";
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
                                            <td className="table-date">{reporterFirst}</td>
                                            <td>
                                                <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                                            </td>
                                            <td className="table-date">{dateStr}</td>
                                            <td>
                                                <div className="table-actions">
                                                    <button
                                                        className="action-btn action-edit"
                                                        onClick={() => navigate(`/items/${item.id}`)}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button
                                                        className="action-btn action-delete"
                                                        onClick={() => setDeleteTarget(item)}
                                                    >
                                                        Delete
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Delete confirmation modal */}
            {deleteTarget && (
                <div className="modal-overlay" onClick={() => setDeleteTarget(null)}>
                    <div className="modal-card" onClick={e => e.stopPropagation()}>
                        <div className="modal-icon-wrap" style={{ background: "#FEE2E2" }}>
                            <span className="modal-icon">🗑</span>
                        </div>
                        <h3 className="modal-title">Delete Item?</h3>
                        <p className="modal-body">
                            Are you sure you want to delete <strong>"{deleteTarget.name}"</strong>? This action cannot be undone.
                        </p>
                        <div className="modal-actions">
                            <button
                                className="btn-logout-confirm"
                                onClick={handleDelete}
                                disabled={deleting}
                            >
                                {deleting ? "Deleting..." : "Yes, Delete"}
                            </button>
                            <button className="btn-modal-cancel" onClick={() => setDeleteTarget(null)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </AdminLayout>
    );
}

export default AdminItemsPage;
