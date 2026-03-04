import { useForm } from "react-hook-form";
import { useNavigate, Link } from "react-router-dom";
import api from "../utils/api";

function Register() {
    const { register, handleSubmit, watch, formState: { errors } } = useForm();
    const navigate = useNavigate();
    const password = watch("password");

    const onSubmit = async (data) => {
        try {
            await api.post("/auth/register", {
                fullName: data.fullName,
                email: data.email,
                password: data.password,
                confirmPassword: data.confirmPassword
            });
            alert("Registration successful! Please log in.");
            navigate("/login");
        } catch (error) {
            alert(error.response?.data?.error?.message || "Registration failed");
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-logo">FINDit</div>
                <p className="auth-tagline">Create your account</p>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="field-group">
                        <label>Full Name</label>
                        <input
                            {...register("fullName", { required: "Full name is required" })}
                            placeholder="Juan Dela Cruz"
                            type="text"
                        />
                        {errors.fullName && <span className="field-error">{errors.fullName.message}</span>}
                    </div>
                    <div className="field-group">
                        <label>Email</label>
                        <input
                            {...register("email", {
                                required: "Email is required",
                                pattern: { value: /^\S+@\S+\.\S+$/, message: "Enter a valid email" }
                            })}
                            placeholder="you@university.edu"
                            type="email"
                        />
                        {errors.email && <span className="field-error">{errors.email.message}</span>}
                    </div>
                    <div className="field-group">
                        <label>Password</label>
                        <input
                            {...register("password", {
                                required: "Password is required",
                                minLength: { value: 8, message: "Password must be at least 8 characters" }
                            })}
                            placeholder="Min. 8 characters"
                            type="password"
                        />
                        {errors.password && <span className="field-error">{errors.password.message}</span>}
                    </div>
                    <div className="field-group">
                        <label>Confirm Password</label>
                        <input
                            {...register("confirmPassword", {
                                required: "Please confirm your password",
                                validate: value => value === password || "Passwords do not match"
                            })}
                            placeholder="••••••••"
                            type="password"
                        />
                        {errors.confirmPassword && <span className="field-error">{errors.confirmPassword.message}</span>}
                    </div>
                    <button type="submit" className="btn-primary">Create Account</button>
                </form>
                <div className="auth-divider"><span>or</span></div>
                <button className="btn-google" onClick={() => window.location.href = "http://localhost:8080/oauth2/authorization/google"}>
                    <svg width="16" height="16" viewBox="0 0 24 24">
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/>
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                    </svg>
                    Continue with Google
                </button>
                <p className="auth-switch">Already have an account? <Link to="/login">Login</Link></p>
            </div>
        </div>
    );
}

export default Register;