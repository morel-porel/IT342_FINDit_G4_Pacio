import { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";
import api from "../utils/api";

function ItemDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const justCreated = location.state?.justCreated;

    useEffect(() => {
        api.get(`/items/${id}`)
            .then(res => setItem(res.data))
            .catch(() => setError("Item not found."))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-ghost" onClick={() => navigate("/dashboard")}>← Back</button>
                </div>
            </nav>
            <p className="loading-text">Loading...</p>
        </div>
    );

    if (error) return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-ghost" onClick={() => navigate("/dashboard")}>← Back</button>
                </div>
            </nav>
            <p className="error-text" style={{ padding: 40 }}>{error}</p>
        </div>
    );

    const isLost = item.type === "LOST";
    const isOwner = user?.id === item.reporter?.id;
    const dateStr = new Date(item.dateLostFound).toLocaleDateString("en-US", {
        weekday: "long", year: "numeric", month: "long", day: "numeric"
    });
    const createdStr = new Date(item.createdAt).toLocaleString("en-US", {
        month: "short", day: "numeric", year: "numeric", hour: "2-digit", minute: "2-digit"
    });

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-ghost" onClick={() => navigate("/dashboard")}>← Back</button>
                </div>
            </nav>

            <div className="form-page-wrapper">
                {justCreated && (
                    <div className="alert alert-success">
                        ✅ Your report was submitted successfully!
                    </div>
                )}
                <div className="detail-card">
                    {item.imageUrl ? (
                        <div className="detail-img">
                            <img src={item.imageUrl} alt={item.name} />
                        </div>
                    ) : (
                        <div className="detail-img detail-img-placeholder">
                            <span>📦</span>
                        </div>
                    )}

                    <div className="detail-body">
                        <div className="detail-badges">
                            <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                                {item.type}
                            </span>
                            <span className="item-card-category">{item.category}</span>
                            <span className={`status-badge status-${item.status?.toLowerCase()}`}>
                                {item.status}
                            </span>
                        </div>

                        <h2 className="detail-title">{item.name}</h2>

                        <div className="detail-meta-grid">
                            <div className="detail-meta-item">
                                <span className="detail-meta-label">📍 Location</span>
                                <span className="detail-meta-value">{item.location}</span>
                            </div>
                            <div className="detail-meta-item">
                                <span className="detail-meta-label">🗓 Date {isLost ? "Lost" : "Found"}</span>
                                <span className="detail-meta-value">{dateStr}</span>
                            </div>
                            <div className="detail-meta-item">
                                <span className="detail-meta-label">👤 Reported By</span>
                                <span className="detail-meta-value">
                                    {item.reporter?.fullName || "Unknown"}
                                    {isOwner && <span className="you-badge"> (You)</span>}
                                </span>
                            </div>
                            <div className="detail-meta-item">
                                <span className="detail-meta-label">🕐 Posted</span>
                                <span className="detail-meta-value">{createdStr}</span>
                            </div>
                        </div>

                        {!isOwner && (
                            <div className="detail-actions">
                                <p className="contact-prompt">
                                    {isLost
                                        ? "Do you know where this item is? Contact the reporter."
                                        : "Is this your item? Submit a claim."}
                                </p>
                                <button
                                    className="btn-primary btn-sm"
                                    onClick={() => alert("Claim submission is coming in Phase 4.")}
                                >
                                    {isLost ? "I Know Where It Is" : "This Is Mine"}
                                </button>
                            </div>
                        )}
                        {isOwner && (
                            <div className="detail-actions">
                                <p className="contact-prompt owner-note">This is your report.</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ItemDetailPage;