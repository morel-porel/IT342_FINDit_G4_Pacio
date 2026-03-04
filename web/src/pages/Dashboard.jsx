import { useNavigate } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";

function Dashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();

    const firstName = user?.fullName?.split(" ")[0] || "there";

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                        + Report Item
                    </button>
                    <div className="avatar" onClick={() => navigate("/profile")}>
                        {user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?"}
                    </div>
                </div>
            </nav>
            <div className="dashboard-welcome">
                <h2>Welcome back, {firstName}!</h2>
                <p>Browse lost and found items below, or report something you've found.</p>
            </div>
        </div>
    );
}

export default Dashboard;