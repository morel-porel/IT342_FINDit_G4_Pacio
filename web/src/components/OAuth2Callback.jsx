import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";
import api from "../utils/api";

function OAuth2Callback() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { login } = useAuth();

    useEffect(() => {
        const token = searchParams.get("token");
        if (!token) {
            navigate("/login");
            return;
        }
        // Fetch user profile with the token
        api.get("/auth/me", {
            headers: { Authorization: `Bearer ${token}` }
        })
        .then(res => {
            login(token, res.data);
            navigate("/dashboard");
        })
        .catch(() => navigate("/login"));
    }, []);

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-logo">FINDit</div>
                <p className="auth-tagline">Signing you in...</p>
            </div>
        </div>
    );
}

export default OAuth2Callback;