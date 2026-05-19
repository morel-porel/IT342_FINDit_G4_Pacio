import { useState, useEffect } from "react";
import api from "../../shared/api/api";
import AdminLayout from "./AdminLayout";

// ADMIN-04 — User Management

function AdminUsersPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionLoading, setActionLoading] = useState(null); // userId being acted on

    useEffect(() => {
        api.get("/admin/users")
            .then(res => setUsers(res.data))
            .catch(() => setError("Failed to load users."))
            .finally(() => setLoading(false));
    }, []);

    const handleDeactivate = async (userId) => {
        setActionLoading(userId);
        try {
            await api.put(`/admin/users/${userId}`, { isActive: false });
            setUsers(prev => prev.map(u =>
                u.id === userId ? { ...u, isActive: false } : u
            ));
        } catch {
            alert("Failed to deactivate user. Please try again.");
        } finally {
            setActionLoading(null);
        }
    };

    const handleActivate = async (userId) => {
        setActionLoading(userId);
        try {
            await api.put(`/admin/users/${userId}`, { isActive: true });
            setUsers(prev => prev.map(u =>
                u.id === userId ? { ...u, isActive: true } : u
            ));
        } catch {
            alert("Failed to activate user. Please try again.");
        } finally {
            setActionLoading(null);
        }
    };

    return (
        <AdminLayout>
            <div className="admin-content">
                <h1 className="admin-page-title">User Management</h1>

                {loading && <p className="loading-text">Loading...</p>}
                {error   && <p className="error-text">{error}</p>}

                {!loading && !error && (
                    <div className="reports-table-wrapper">
                        <table className="reports-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="table-empty">No users found.</td>
                                    </tr>
                                ) : users.map(u => {
                                    const isAdmin = u.role === "ADMIN";
                                    const isActive = u.isActive !== false; // default true
                                    const isActing = actionLoading === u.id;
                                    return (
                                        <tr key={u.id}>
                                            <td>
                                                <div style={{ fontWeight: 600, color: "var(--dark)", fontSize: 13 }}>
                                                    {u.fullName}
                                                </div>
                                            </td>
                                            <td className="table-date">{u.email}</td>
                                            <td>
                                                <span className={`role-badge ${isAdmin ? "role-badge-admin" : "role-badge-user"}`}>
                                                    {u.role}
                                                </span>
                                            </td>
                                            <td>
                                                <span className={`status-badge ${isActive ? "status-approved" : "status-rejected"}`}>
                                                    {isActive ? "Active" : "Inactive"}
                                                </span>
                                            </td>
                                            <td>
                                                {/* Admin accounts cannot be deactivated */}
                                                {isAdmin ? (
                                                    <span className="table-dash">—</span>
                                                ) : isActive ? (
                                                    <button
                                                        className="action-btn action-delete"
                                                        disabled={isActing}
                                                        onClick={() => handleDeactivate(u.id)}
                                                    >
                                                        {isActing ? "..." : "Deactivate"}
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="action-btn admin-approve-btn"
                                                        disabled={isActing}
                                                        onClick={() => handleActivate(u.id)}
                                                    >
                                                        {isActing ? "..." : "Activate"}
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </AdminLayout>
    );
}

export default AdminUsersPage;
