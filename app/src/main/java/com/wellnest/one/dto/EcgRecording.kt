package com.wellnest.one.dto

import com.wellnest.one.model.response.*

/**
 * Created by Hussain on 23/11/22.
 */
data class EcgRecording(
    val acceptedByDoctor: AcceptedByDoctor? = null,
    val addSignature: Boolean? = null,
    val age: Int? = null,
    val aiAction: String? = null,
    val aiResponse: String? = null,
    val bpm: Int? = null,
    val canForward: Boolean? = null,
    val cardiologyAdvice: String? = null,
    val createdAt: String? = null,
    val ecgFindings: String? = null,
    val fileName: String? = null,
    val forwardCount: Int? = null,
    val gender: String? = null,
    val hasReported: Boolean? = null,
    val id: Int? = null,
    val interpretations: String? = null,
    val patient: Patient? = null,
    val pendingApproval: Boolean? = null,
    val pr: Double? = null,
    val qrs: Double? = null,
    val qt: Double? = null,
    val qtc: Double? = null,
    val reason: String? = null,
    val recommendations: String? = null,
    val referredBy: ReferredBy? = null,
    val reportedBy: ReportedBy? = null,
    val reportedById: Int? = null,
    val reviewStatus: String? = null,
    val risk: String? = null,
    val setup: String? = null,
    val st: Double? = null
) {
    fun getPatientName(): String {
        if (patient?.patientLastName != null && patient.patientLastName.isNotEmpty()) {
            return "${patient.patientFirstName!!.capitalize()} ${patient.patientLastName!!.capitalize()}"
        } else {
            return "${patient?.patientFirstName}"
        }
    }
}
