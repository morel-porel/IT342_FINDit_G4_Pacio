import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";
import api from "../utils/api";

function MyReportsPage() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        api.get("/items/my")
            .then(res => setItems(res.data))
            .catch(() => setError("Failed to load your reports."))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-ghost" onClick={() => navigate("/dashboard")}>← Dashboard</button>
                    <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                        + Report Item
                    </button>
                </div>
            </nav>

            <div className="form-page-wrapper" style={{ maxWidth: 720 }}>
                <h2 className="form-title" style={{ marginBottom: 8 }}>My Reports</h2>
                <p className="form-subtitle">Items you've reported as lost or found.</p>

                {loading && <p className="loading-text">Loading...</p>}
                {error && <p className="error-text">{error}</p>}
                {!loading && !error && items.length === 0 && (
                    <div className="empty-state">
                        <span>📋</span>
                        <p>You haven't reported any items yet.</p>
                        <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                            Report Your First Item
                        </button>
                    </div>
                )}

                <div className="my-reports-list">
                    {items.map(item => {
                        const isLost = item.type === "LOST";
                        const date = new Date(item.dateLostFound).toLocaleDateString("en-US", {
                            month: "short", day: "numeric", year: "numeric"
                        });
                        return (
                            <div
                                key={item.id}
                                className="report-row"
                                onClick={() => navigate(`/items/${item.id}`)}
                            >
                                <div className="report-row-left">
                                    <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                                        {item.type}
                                    </span>
                                    <div>
                                        <div className="report-row-name">{item.name}</div>
                                        <div className="report-row-meta">{item.category} · {item.location} · {date}</div>
                                    </div>
                                </div>
                                <div className="report-row-right">
                                    <span className={`status-badge status-${item.status?.toLowerCase()}`}>
                                        {item.status}
                                    </span>
                                    <span className="chevron">›</span>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}

export default MyReportsPage;