import { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";
import api from "../utils/api";

const CATEGORY_ICONS = {
    "Electronics": "💻", "Clothing": "👕", "Accessories": "🎒",
    "Documents": "📄", "Keys": "🔑", "Wallet / Bag": "👜",
    "Books": "📚", "Sports": "⚽", "Other": "📦",
};

const STATUS_META = {
    "OPEN":     { label: "Open",     cls: "status-open" },
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
    "RESOLVED": { label: "Resolved", cls: "status-resolved" },
};

function ItemDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [claimText, setClaimText] = useState("");
    const [claimSubmitted, setClaimSubmitted] = useState(false);
    const justCreated = location.state?.justCreated;

    const initials = user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?";

    useEffect(() => {
        api.get(`/items/${id}`)
            .then(res => setItem(res.data))
            .catch(() => setError("Item not found."))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return (
        <div className="dashboard-page">
            <nav className="topbar topbar-simple">
                <button className="btn-back" onClick={() => navigate("/dashboard")}>← Back</button>
                <div className="topbar-logo">FINDit</div>
                <div className="avatar">{initials}</div>
            </nav>
            <p className="loading-text">Loading...</p>
        </div>
    );

    if (error) return (
        <div className="dashboard-page">
            <nav className="topbar topbar-simple">
                <button className="btn-back" onClick={() => navigate("/dashboard")}>← Back</button>
                <div className="topbar-logo">FINDit</div>
                <div className="avatar">{initials}</div>
            </nav>
            <p className="error-text" style={{ padding: 40 }}>{error}</p>
        </div>
    );

    const isLost = item.type === "LOST";
    const isOwner = user?.id === item.reporter?.id;
    const icon = CATEGORY_ICONS[item.category] || "📦";
    const sm = STATUS_META[item.status] || { label: item.status, cls: "status-open" };

    const dateStr = new Date(item.dateLostFound).toLocaleDateString("en-US", {
        month: "long", day: "numeric", year: "numeric"
    });
    const reporterFirst = item.reporter?.fullName?.split(" ")[0] || "Unknown";

    return (
        <div className="dashboard-page">
            {/* ── Header: ← Back  FINDit  avatar ── */}
            <nav className="topbar topbar-simple">
                <button className="btn-back" onClick={() => navigate("/dashboard")}>← Back</button>
                <div className="topbar-logo">FINDit</div>
                <div className="avatar" onClick={() => navigate("/profile")}>{initials}</div>
            </nav>

            {justCreated && (
                <div className="alert alert-success" style={{ margin: "12px 24px 0" }}>
                    ✅ Your report was submitted successfully!
                </div>
            )}

            {/* ── Split layout: image left, details right ── */}
            <div className="detail-layout">
                {/* Left — large image */}
                <div className="detail-image-pane">
                    {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.name} className="detail-main-img" />
                    ) : (
                        <div className="detail-img-placeholder">
                            <span>{icon}</span>
                        </div>
                    )}
                </div>

                {/* Right — details */}
                <div className="detail-info-pane">
                    {/* Badges row */}
                    <div className="detail-badges">
                        <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                            {isLost ? "Lost" : "Found"}
                        </span>
                        <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                        <span className="category-tag">{item.category}</span>
                    </div>

                    {/* Item name */}
                    <h2 className="detail-title">{item.name}</h2>

                    {/* Meta line */}
                    <div className="detail-meta-line">
                        <span>📍 {item.location}</span>
                        <span>🗓 {dateStr}</span>
                        <span>👤 Reported by {reporterFirst}{isOwner ? " (You)" : ""}</span>
                    </div>

                    {/* Description */}
                    {item.description && (
                        <p className="detail-description">{item.description}</p>
                    )}

                    {/* Info rows */}
                    <div className="detail-info-rows">
                        <div className="detail-info-row">
                            <span className="detail-info-label">Category</span>
                            <span className="detail-info-value">{item.category}</span>
                        </div>
                        <div className="detail-info-row">
                            <span className="detail-info-label">Location</span>
                            <span className="detail-info-value">{item.location}</span>
                        </div>
                        <div className="detail-info-row">
                            <span className="detail-info-label">
                                Date {isLost ? "Lost" : "Found"}
                            </span>
                            <span className="detail-info-value">{dateStr}</span>
                        </div>
                    </div>

                    {/* Weather card */}
                    {item.weatherContext && (
                        <div className="weather-card">
                            <span className="weather-icon">🌤</span>
                            <div>
                                <div className="weather-label">Weather at time of report</div>
                                <div className="weather-value">{item.weatherContext}</div>
                            </div>
                        </div>
                    )}

                    {/* Claim panel — only shown to non-owners on OPEN items */}
                    {!isOwner && item.status === "OPEN" && (
                        <div className="claim-panel">
                            {claimSubmitted ? (
                                <p className="claim-submitted">
                                    ✅ Your claim has been submitted. The admin will review it.
                                </p>
                            ) : (
                                <>
                                    <div className="claim-panel-title">Is this yours?</div>
                                    <p className="claim-panel-sub">
                                        Describe how you can prove ownership of this item.
                                    </p>
                                    <textarea
                                        className="claim-textarea"
                                        rows={3}
                                        placeholder="e.g. The phone has my initials on the back, lockscreen is..."
                                        value={claimText}
                                        onChange={e => setClaimText(e.target.value)}
                                    />
                                    <button className="claim-attach">
                                        + Attach Proof Photos <span className="optional">(optional)</span>
                                    </button>
                                    <button
                                        className="btn-primary btn-sm claim-submit-btn"
                                        onClick={() => {
                                            if (claimText.trim()) setClaimSubmitted(true);
                                        }}
                                    >
                                        Submit Claim
                                    </button>
                                </>
                            )}
                        </div>
                    )}

                    {isOwner && (
                        <div className="owner-note-box">
                            This is your report.
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ItemDetailPage;