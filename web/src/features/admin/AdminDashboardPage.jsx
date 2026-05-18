import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../shared/api/api";
import AdminLayout from "./AdminLayout";

// ADMIN-01 — Dashboard Overview

const STATUS_META = {
    "OPEN":     { label: "Open",     cls: "status-open" },
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
    "RESOLVED": { label: "Resolved", cls: "status-resolved" },
};

function StatCard({ value, label }) {
    return (
        <div className="stat-card">
            <div className="stat-value">{value}</div>
            <div className="stat-label">{label}</div>
        </div>
    );
}

function AdminDashboardPage() {
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            api.get("/items"),
            api.get("/claims"),
        ]).then(([itemsRes, claimsRes]) => {
            setItems(itemsRes.data);
            setClaims(claimsRes.data);
        }).catch(() => {}).finally(() => setLoading(false));
    }, []);

    const totalItems    = items.length;
    const openItems     = items.filter(i => i.status === "OPEN").length;
    const pendingClaims = claims.filter(c => c.status === "PENDING").length;
    const resolvedItems = items.filter(i => i.status === "RESOLVED").length;

    // Recent activity: last 10 items sorted by createdAt desc
    const recentItems = [...items]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 10);

    return (
        <AdminLayout>
            <div className="admin-content">
                <h1 className="admin-page-title">Dashboard Overview</h1>

                {loading ? (
                    <p className="loading-text">Loading...</p>
                ) : (
                    <>
                        <div className="stat-grid">
                            <StatCard value={totalItems}    label="Total Items" />
                            <StatCard value={openItems}     label="Open Items" />
                            <StatCard value={pendingClaims} label="Pending Claims" />
                            <StatCard value={resolvedItems} label="Resolved Items" />
                        </div>

                        <div className="admin-section">
                            <h2 className="admin-section-title">Recent Activity</h2>
                            <div className="reports-table-wrapper">
                                <table className="reports-table">
                                    <thead>
                                        <tr>
                                            <th>Item</th>
                                            <th>Type</th>
                                            <th>Reported By</th>
                                            <th>Status</th>
                                            <th>Date</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {recentItems.length === 0 ? (
                                            <tr>
                                                <td colSpan={5} className="table-empty">No activity yet.</td>
                                            </tr>
                                        ) : recentItems.map(item => {
                                            const isLost = item.type === "LOST";
                                            const sm = STATUS_META[item.status] || { label: item.status, cls: "status-open" };
                                            const dateStr = new Date(item.createdAt || item.dateLostFound).toLocaleDateString("en-US", {
                                                month: "short", day: "numeric"
                                            });
                                            const reporterFirst = item.reporter?.fullName?.split(" ")[0] || "Unknown";
                                            return (
                                                <tr key={item.id} style={{ cursor: "pointer" }}
                                                    onClick={() => navigate(`/items/${item.id}`)}>
                                                    <td>
                                                        <div className="table-item-name">{item.name}</div>
                                                        <div className="table-item-loc">📍 {item.location}</div>
                                                    </td>
                                                    <td>
                                                        <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                                                            {isLost ? "Lost" : "Found"}
                                                        </span>
                                                    </td>
                                                    <td className="table-date">{reporterFirst}</td>
                                                    <td>
                                                        <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                                                    </td>
                                                    <td className="table-date">{dateStr}</td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </AdminLayout>
    );
}

export default AdminDashboardPage;
