import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import api from "../../shared/api/api";

// WEB-06 — My Claims

const STATUS_META = {
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
};

function MyClaimsPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const initials = user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?";

    useEffect(() => {
        api.get("/claims")
            .then(res => setClaims(res.data))
            .catch(() => setError("Failed to load your claims."))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo" onClick={() => navigate("/dashboard")}>FINDit</div>
                <div className="topbar-actions">
                    <button className="topnav-btn" onClick={() => navigate("/my-reports")}>My Reports</button>
                    <button className="topnav-btn topnav-btn-active">My Claims</button>
                    <div className="avatar" onClick={() => navigate("/profile")}>{initials}</div>
                </div>
            </nav>

            <div className="my-reports-page">
                <div className="my-reports-header">
                    <h2 className="my-reports-title">My Claims</h2>
                </div>

                {loading && <p className="loading-text">Loading...</p>}
                {error   && <p className="error-text">{error}</p>}

                {!loading && !error && (
                    claims.length === 0 ? (
                        <div className="empty-state">
                            <span>🔖</span>
                            <p>You haven't submitted any claims yet.</p>
                            <button className="btn-primary btn-sm" onClick={() => navigate("/dashboard")}>
                                Browse Items
                            </button>
                        </div>
                    ) : (
                        <div className="reports-table-wrapper">
                            <table className="reports-table">
                                <thead>
                                    <tr>
                                        <th>Item</th>
                                        <th>Submitted</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {claims.map(claim => {
                                        const sm = STATUS_META[claim.status] || { label: claim.status, cls: "status-pending" };
                                        const submitted = new Date(claim.createdAt).toLocaleDateString("en-US", {
                                            month: "short", day: "numeric", year: "numeric"
                                        });
                                        return (
                                            <tr key={claim.id}>
                                                <td>
                                                    <div
                                                        className="table-item-name"
                                                        onClick={() => navigate(`/items/${claim.item?.id}`)}
                                                    >
                                                        {claim.item?.name || "Unknown Item"}
                                                    </div>
                                                    {claim.item?.location && (
                                                        <div className="table-item-loc">📍 {claim.item.location}</div>
                                                    )}
                                                </td>
                                                <td className="table-date">{submitted}</td>
                                                <td>
                                                    <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    )
                )}
            </div>
        </div>
    );
}

export default MyClaimsPage;
