import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../shared/api/api";
import AdminLayout from "./AdminLayout";

// ADMIN-03 — Claim Review

const STATUS_META = {
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
};

function normalizeClaim(c) {
    return {
        ...c,
        item: {
            id:           c.itemId,
            name:         c.itemName,
            location:     c.itemLocation,
            dateLostFound: c.itemDateLostFound ?? null,
            description:  c.itemDescription ?? null,
        },
        claimant: {
            id:       c.claimantId,
            fullName: c.claimantName,
            email:    c.claimantEmail,
        },
    };
}

function AdminClaimsPage() {
    const navigate = useNavigate();
    const [claims, setClaims] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null); // currently expanded claim
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        api.get("/claims")
            .then(res => {
                const normalizedClaims = res.data.map(normalizeClaim);
                setClaims(normalizedClaims);
                // Auto-select first pending claim if any
                const firstPending = normalizedClaims.find(c => c.status === "PENDING");
                if (firstPending) setSelected(firstPending);
            })
            .catch(() => setError("Failed to load claims."))
            .finally(() => setLoading(false));
    }, []);

    const handleAction = async (claimId, action) => {
        setActionLoading(true);
        try {
            await api.put(`/claims/${claimId}/${action}`);
            // Refresh claims list
            const res = await api.get("/claims");
            const normalizedClaims = res.data.map(normalizeClaim);
            setClaims(normalizedClaims);
            // Update selected to reflect new status
            const updated = normalizedClaims.find(c => c.id === claimId);
            if (updated) setSelected(updated);
        } catch {
            alert(`Failed to ${action} claim. Please try again.`);
        } finally {
            setActionLoading(false);
        }
    };

    const pendingClaims = claims.filter(c => c.status === "PENDING");
    const allClaims     = claims;

    const formatDate = (dateStr) => dateStr
        ? new Date(dateStr).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })
        : "—";

    return (
        <AdminLayout>
            <div className="admin-content">
                <h1 className="admin-page-title">Claim Review</h1>

                {loading && <p className="loading-text">Loading...</p>}
                {error   && <p className="error-text">{error}</p>}

                {!loading && !error && (
                    <>
                        {/* Claims table */}
                        <div className="reports-table-wrapper" style={{ marginBottom: 24 }}>
                            <table className="reports-table">
                                <thead>
                                    <tr>
                                        <th>Claim ID</th>
                                        <th>Found Item</th>
                                        <th>Claimant</th>
                                        <th>Submitted</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {allClaims.length === 0 ? (
                                        <tr>
                                            <td colSpan={6} className="table-empty">No claims yet.</td>
                                        </tr>
                                    ) : allClaims.map(claim => {
                                        const sm = STATUS_META[claim.status] || { label: claim.status, cls: "status-pending" };
                                        const isPending = claim.status === "PENDING";
                                        const isSelected = selected?.id === claim.id;
                                        return (
                                            <tr
                                                key={claim.id}
                                                style={{ cursor: "pointer", background: isSelected ? "#EFF6FF" : undefined }}
                                                onClick={() => setSelected(claim)}
                                            >
                                                <td>
                                                    <span style={{ fontFamily: "DM Mono, monospace", fontSize: 12, color: "var(--slate)" }}>
                                                        #CLM-{String(claim.id).padStart(3, "0")}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div
                                                        className="table-item-name"
                                                        onClick={e => { e.stopPropagation(); navigate(`/items/${claim.item?.id}`); }}
                                                    >
                                                        {claim.item?.name || "Unknown Item"}
                                                    </div>
                                                    {claim.item?.location && (
                                                        <div className="table-item-loc">
                                                            📍 {claim.item.location} · {formatDate(claim.item?.dateLostFound)}
                                                        </div>
                                                    )}
                                                </td>
                                                <td className="table-date">
                                                    {claim.claimant?.fullName?.split(" ")[0] || "Unknown"}
                                                    {claim.claimant?.fullName?.split(" ")[1]
                                                        ? " " + claim.claimant.fullName.split(" ")[1][0] + "."
                                                        : ""}
                                                </td>
                                                <td className="table-date">{formatDate(claim.createdAt)}</td>
                                                <td>
                                                    <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
                                                </td>
                                                <td onClick={e => e.stopPropagation()}>
                                                    {isPending ? (
                                                        <div className="table-actions">
                                                            <button
                                                                className="action-btn admin-approve-btn"
                                                                disabled={actionLoading}
                                                                onClick={() => handleAction(claim.id, "approve")}
                                                            >
                                                                ✓ Approve
                                                            </button>
                                                            <button
                                                                className="action-btn action-delete"
                                                                disabled={actionLoading}
                                                                onClick={() => handleAction(claim.id, "reject")}
                                                            >
                                                                ✗ Reject
                                                            </button>
                                                        </div>
                                                    ) : (
                                                        <span className="table-dash">—</span>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>

                        {/* Expanded claim detail panel */}
                        {selected && (
                            <div className="claim-detail-panel">
                                <h2 className="claim-detail-title">
                                    Claim Detail — <span style={{ fontFamily: "DM Mono, monospace" }}>
                                        #CLM-{String(selected.id).padStart(3, "0")}
                                    </span>
                                </h2>
                                <div className="claim-detail-grid">
                                    {/* Claimant card */}
                                    <div className="claim-detail-card">
                                        <div className="claim-detail-card-label">Claimant</div>
                                        <div className="claim-detail-card-name">{selected.claimant?.fullName || "—"}</div>
                                        <div className="claim-detail-card-sub">{selected.claimant?.email || "—"}</div>
                                        <div className="claim-detail-card-sub" style={{ marginTop: 8 }}>
                                            Submitted {formatDate(selected.createdAt)}
                                        </div>
                                    </div>

                                    {/* Found item card */}
                                    <div className="claim-detail-card">
                                        <div className="claim-detail-card-label">Found Item Report</div>
                                        <div
                                            className="table-item-name"
                                            style={{ marginBottom: 4 }}
                                            onClick={() => navigate(`/items/${selected.item?.id}`)}
                                        >
                                            {selected.item?.name || "—"} ↗
                                        </div>
                                        {selected.item?.location && (
                                            <div className="claim-detail-card-sub">📍 {selected.item.location}</div>
                                        )}
                                        {selected.item?.dateLostFound && (
                                            <div className="claim-detail-card-sub">
                                                🗓 {formatDate(selected.item.dateLostFound)}
                                            </div>
                                        )}
                                        {selected.item?.description && (
                                            <p className="claim-detail-card-desc">"{selected.item.description}"</p>
                                        )}
                                    </div>

                                    {/* Claimant's proof */}
                                    <div className="claim-detail-card">
                                        <div className="claim-detail-card-label">Claimant's Proof</div>
                                        <p className="claim-detail-card-desc">
                                            "{selected.proofDescription || "No description provided."}"
                                        </p>
                                        {selected.proofImageUrl && (
                                            <img
                                                src={selected.proofImageUrl}
                                                alt="Proof"
                                                style={{ marginTop: 10, width: "100%", borderRadius: 8, maxHeight: 160, objectFit: "cover" }}
                                            />
                                        )}
                                    </div>
                                </div>

                                {/* Bottom action buttons (only for PENDING) */}
                                {selected.status === "PENDING" && (
                                    <div className="claim-detail-actions">
                                        <button
                                            className="claim-action-approve"
                                            disabled={actionLoading}
                                            onClick={() => handleAction(selected.id, "approve")}
                                        >
                                            ✓ Approve Claim
                                        </button>
                                        <button
                                            className="claim-action-reject"
                                            disabled={actionLoading}
                                            onClick={() => handleAction(selected.id, "reject")}
                                        >
                                            ✗ Reject Claim
                                        </button>
                                        <span className="claim-action-note">
                                            Approving will set item status to RESOLVED and notify the claimant via email.
                                        </span>
                                    </div>
                                )}
                            </div>
                        )}
                    </>
                )}
            </div>
        </AdminLayout>
    );
}

export default AdminClaimsPage;
