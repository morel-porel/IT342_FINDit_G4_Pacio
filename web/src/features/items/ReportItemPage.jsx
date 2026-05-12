import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import api from "../../shared/api/api";

const CATEGORIES = [
    "Electronics", "Clothing", "Accessories", "Documents",
    "Keys", "Wallet / Bag", "Books", "Sports", "Other",
];

function ReportItemPage() {
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors }, watch } = useForm({
        defaultValues: { type: "FOUND" }
    });
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const [photoFile, setPhotoFile] = useState(null);
    const [photoPreview, setPhotoPreview] = useState(null);
    const selectedType = watch("type");

    const handlePhotoChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setPhotoFile(file);
        setPhotoPreview(URL.createObjectURL(file));
    };

    const onSubmit = async (data) => {
        setSubmitting(true);
        setSubmitError(null);
        try {
            // In a full implementation, upload photo to storage and get URL.
            // For now, we send null for imageUrl if no file is uploaded.
            const payload = {
                type: data.type,
                name: data.name.trim(),
                category: data.category,
                description: data.description?.trim() || null,
                dateLostFound: data.dateLostFound,
                location: data.location.trim(),
                imageUrl: null, // Photo upload URL would go here after storage integration
            };
            const response = await api.post("/items", payload);
            navigate(`/items/${response.data.id}`, { state: { justCreated: true } });
        } catch (err) {
            setSubmitError(err.response?.data || "Something went wrong. Please try again.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="dashboard-page">
            {/* ── Header: ← Back  FINDit ── */}
            <nav className="topbar topbar-simple">
                <button className="btn-back" onClick={() => navigate("/dashboard")}>
                    ← Back
                </button>
                <div className="topbar-logo">FINDit</div>
            </nav>

            <div className="form-page-wrapper">
                <div className="form-card">
                    <h2 className="form-title">Report an Item</h2>
                    <p className="form-subtitle">Fill in the details to post a lost or found report.</p>

                    {submitError && (
                        <div className="alert alert-error">{submitError}</div>
                    )}

                    <form onSubmit={handleSubmit(onSubmit)}>
                        {/* Report Type — Found left, Lost right, per SDD */}
                        <div className="field-group">
                            <label>Report Type</label>
                            <div className="type-toggle">
                                <label className={`type-option ${selectedType === "FOUND" ? "type-option-active-found" : ""}`}>
                                    <input type="radio" value="FOUND" {...register("type")} />
                                    📢 I Found Something
                                </label>
                                <label className={`type-option ${selectedType === "LOST" ? "type-option-active-lost" : ""}`}>
                                    <input type="radio" value="LOST" {...register("type")} />
                                    🔍 I Lost Something
                                </label>
                            </div>
                        </div>

                        {/* Item Name */}
                        <div className="field-group">
                            <label>Item Name <span className="required">*</span></label>
                            <input
                                {...register("name", {
                                    required: "Item name is required",
                                    maxLength: { value: 100, message: "Max 100 characters" }
                                })}
                                placeholder="e.g. Black Samsung Galaxy A54"
                                type="text"
                            />
                            {errors.name && <span className="field-error">{errors.name.message}</span>}
                        </div>

                        {/* Category */}
                        <div className="field-group">
                            <label>Category <span className="required">*</span></label>
                            <select {...register("category", { required: "Please select a category" })}>
                                <option value="">— Select category —</option>
                                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                            </select>
                            {errors.category && <span className="field-error">{errors.category.message}</span>}
                        </div>

                        {/* Description — comes before date/location per SDD */}
                        <div className="field-group">
                            <label>Description</label>
                            <textarea
                                {...register("description", {
                                    maxLength: { value: 500, message: "Max 500 characters" }
                                })}
                                placeholder="Describe the item in detail — color, brand, markings, condition..."
                                rows={3}
                            />
                            {errors.description && <span className="field-error">{errors.description.message}</span>}
                        </div>

                        {/* Date + Location — two columns */}
                        <div className="field-row-2col">
                            <div className="field-group">
                                <label>
                                    Date {selectedType === "FOUND" ? "Found" : "Lost"} <span className="required">*</span>
                                </label>
                                <input
                                    {...register("dateLostFound", {
                                        required: "Date is required",
                                        validate: v => new Date(v) <= new Date() || "Date cannot be in the future"
                                    })}
                                    type="date"
                                    max={new Date().toISOString().split("T")[0]}
                                />
                                {errors.dateLostFound && <span className="field-error">{errors.dateLostFound.message}</span>}
                            </div>
                            <div className="field-group">
                                <label>Location <span className="required">*</span></label>
                                <input
                                    {...register("location", {
                                        required: "Location is required",
                                        maxLength: { value: 200, message: "Max 200 characters" }
                                    })}
                                    placeholder="e.g. Library, 2nd Floor"
                                    type="text"
                                />
                                {errors.location && <span className="field-error">{errors.location.message}</span>}
                            </div>
                        </div>

                        {/* Photo upload — required for found items */}
                        <div className="field-group">
                            <label>
                                Photo{" "}
                                <span className={selectedType === "FOUND" ? "required" : "optional"}>
                                    {selectedType === "FOUND" ? "(required for found items)" : "(optional)"}
                                </span>
                            </label>
                            {photoPreview ? (
                                <div className="photo-preview">
                                    <img src={photoPreview} alt="Preview" />
                                    <button type="button" className="photo-remove"
                                        onClick={() => { setPhotoFile(null); setPhotoPreview(null); }}>
                                        ✕ Remove
                                    </button>
                                </div>
                            ) : (
                                <label className="photo-upload-zone">
                                    <input
                                        type="file"
                                        accept="image/jpeg,image/png,image/webp"
                                        style={{ display: "none" }}
                                        onChange={handlePhotoChange}
                                    />
                                    <span className="photo-upload-icon">🖼</span>
                                    <span className="photo-upload-text">Click to upload or drag and drop</span>
                                    <span className="photo-upload-hint">JPG, PNG · Max 5MB</span>
                                </label>
                            )}
                        </div>

                        <button type="submit" className="btn-primary" disabled={submitting}>
                            {submitting ? "Submitting..." : "Submit Report"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default ReportItemPage;