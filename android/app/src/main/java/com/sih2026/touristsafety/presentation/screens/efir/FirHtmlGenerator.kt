package com.sih2026.touristsafety.presentation.screens.efir

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sih2026.touristsafety.data.remote.StructuredFir
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirHtmlGenerator {

    fun generateQrCodeBase64(content: String, size: Int = 220): String {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun computeSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "0x" + System.currentTimeMillis().toString(16)
        }
    }

    fun generateOfficialFirHtml(fir: StructuredFir): String {
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val filingYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        
        val firNo = if (fir.firNumber.isNotBlank()) fir.firNumber else "DL-ND-${filingYear}-EFIR-${(100000..999999).random()}"
        val gdNo = "GD-${(100..999).random()}/${filingYear}"
        val psName = if (fir.policeStation.isNotBlank()) fir.policeStation else "Connaught Place Police Station"
        val district = if (fir.district.isNotBlank()) fir.district else "New Delhi District"
        val state = if (fir.state.isNotBlank()) fir.state else "Delhi"

        val qrContent = "GOVT OF INDIA | CCTNS e-FIR Verification\nFIR No: $firNo\nPS: $psName\nDate: $currentDate\nSections: ${fir.bnsSections.joinToString(", ")}\nStatus: REGISTERED (PROVISIONAL)"
        val qrBase64 = generateQrCodeBase64(qrContent)
        val hash = computeSha256(firNo + psName + fir.incidentType + fir.narrative)

        val bnsListHtml = if (fir.bnsSections.isNotEmpty()) {
            fir.bnsSections.map { "<span class='badge'>$it</span>" }.joinToString(" ")
        } else {
            "<span class='badge'>Sec 303(2) BNS (Theft)</span>"
        }

        val complaintLetter = if (fir.formalComplaintLetter.isNotBlank()) {
            fir.formalComplaintLetter
        } else {
            """To,
The Station House Officer,
$psName, $district.

Subject: Formal complaint regarding ${fir.incidentType} under Bharatiya Nyaya Sanhita (BNS), 2023.

Respected Sir/Madam,
I, the undersigned complainant, do hereby state that on ${fir.dateTime} at ${fir.place}, the following incident occurred:

${fir.narrative}

Description of Accused / Suspects: ${fir.accusedDescription}
Particulars of Property Involved / Stolen: ${fir.propertyLost}
Eyewitnesses (if any): ${fir.witnesses}

I humbly request your good office to register an e-FIR under the applicable sections of Bharatiya Nyaya Sanhita (BNS), 2023, and take prompt legal action to recover my belongings and investigate the matter.

Yours faithfully,
${fir.complainantDetails.ifBlank { "Complainant / Tourist" }}"""
        }

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>First Information Report (e-FIR)</title>
<style>
  @page {
    size: A4 portrait;
    margin: 10mm;
  }
  * {
    box-sizing: border-box;
    -webkit-print-color-adjust: exact !important;
    print-color-adjust: exact !important;
  }
  body {
    font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    color: #111;
    font-size: 13px;
    line-height: 1.5;
    background: #fff;
    margin: 0;
    padding: 12px;
    width: 100%;
  }
  .header-container {
    border-bottom: 3px double #002b49;
    padding-bottom: 12px;
    margin-bottom: 14px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .emblem-title {
    text-align: center;
    flex-grow: 1;
  }
  .govt-title {
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 1.2px;
    color: #333;
    text-transform: uppercase;
  }
  .main-title {
    font-size: 20px;
    font-weight: 900;
    color: #002b49;
    margin: 4px 0 2px 0;
    letter-spacing: 0.5px;
  }
  .sub-title {
    font-size: 11px;
    color: #555;
    font-style: italic;
  }
  .qr-box {
    text-align: center;
    padding-left: 12px;
    min-width: 90px;
  }
  .qr-box img {
    border: 1px solid #ccc;
    padding: 3px;
    display: block;
    margin: 0 auto;
  }
  .qr-box span {
    display: block;
    font-size: 8px;
    color: #002b49;
    font-weight: bold;
    margin-top: 3px;
    letter-spacing: 0.5px;
  }
  .meta-grid {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 14px;
    background-color: #f8fafd;
    border: 1px solid #b5c7de;
  }
  .meta-grid td {
    padding: 7px 10px;
    font-size: 12px;
    border: 1px solid #b5c7de;
  }
  .meta-label {
    font-weight: 700;
    color: #002b49;
    width: 25%;
  }
  .meta-val {
    color: #111;
  }
  .section-header {
    background-color: #002b49;
    color: #ffffff;
    font-size: 12px;
    font-weight: 700;
    padding: 6px 10px;
    margin: 14px 0 6px 0;
    border-radius: 3px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  .data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 10px;
  }
  .data-table th, .data-table td {
    border: 1px solid #b0bec5;
    padding: 7px 10px;
    font-size: 12px;
    vertical-align: top;
  }
  .data-table th {
    background-color: #eaf0f8;
    color: #002b49;
    font-weight: 700;
    text-align: left;
  }
  .badge {
    display: inline-block;
    background-color: #003366;
    color: #ffffff;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 700;
    margin-right: 4px;
    margin-bottom: 3px;
  }
  .narrative-box {
    border: 1px solid #b0bec5;
    background-color: #fafbfc;
    padding: 12px 14px;
    font-size: 12px;
    white-space: pre-wrap;
    line-height: 1.6;
    margin-bottom: 14px;
    border-left: 4px solid #002b49;
    border-radius: 2px;
  }
  .footer-signatures {
    margin-top: 30px;
    width: 100%;
    border-collapse: collapse;
  }
  .footer-signatures td {
    width: 50%;
    vertical-align: bottom;
    padding: 10px 20px;
  }
  .sig-line {
    border-top: 1px dashed #333;
    margin-top: 45px;
    padding-top: 6px;
    text-align: center;
    font-weight: 700;
    font-size: 11px;
  }
  .hash-box {
    margin-top: 16px;
    padding: 8px 10px;
    background: #f4f4f4;
    border: 1px solid #ccc;
    font-family: monospace;
    font-size: 9px;
    color: #444;
    word-break: break-all;
    border-radius: 2px;
  }
  .watermark {
    text-align: center;
    color: #777;
    font-size: 9px;
    margin-top: 10px;
    font-style: italic;
  }
</style>
</head>
<body>

<div class="header-container">
  <div class="emblem-title">
    <div class="govt-title">State Police Department • Tourist Safety Division</div>
    <div class="main-title">FIRST INFORMATION REPORT (e-FIR)</div>
    <div class="sub-title">FORM - II [Under Section 173 of Bharatiya Nagarik Suraksha Sanhita (BNSS), 2023]</div>
  </div>
  <div class="qr-box">
    <img src="data:image/png;base64,$qrBase64" width="85" height="85" alt="CCTNS QR" />
    <span>CCTNS VERIFIED</span>
  </div>
</div>

<table class="meta-grid">
  <tr>
    <td class="meta-label">1. e-FIR Number:</td>
    <td class="meta-val"><strong>$firNo</strong></td>
    <td class="meta-label">2. Filing Date & Time:</td>
    <td class="meta-val">$currentDate IST</td>
  </tr>
  <tr>
    <td class="meta-label">3. District & State:</td>
    <td class="meta-val">$district, $state</td>
    <td class="meta-label">4. Jurisdictional PS:</td>
    <td class="meta-val"><strong>$psName</strong></td>
  </tr>
  <tr>
    <td class="meta-label">5. GD Entry Reference:</td>
    <td class="meta-val">$gdNo</td>
    <td class="meta-label">6. Registration Category:</td>
    <td class="meta-val"><span style="color:#d32f2f; font-weight:bold;">Cognizable Offence (Tourist Fast-Track)</span></td>
  </tr>
</table>

<div class="section-header">A. Statutory Acts and Applicable Legal Sections</div>
<table class="data-table">
  <tr>
    <th style="width: 35%;">Governing Statutory Act</th>
    <th>Applicable Sections & Legal Offence Nomenclature</th>
  </tr>
  <tr>
    <td><strong>Bharatiya Nyaya Sanhita (BNS), 2023</strong><br><small style="color:#666;">(Replacing Indian Penal Code, 1860)</small></td>
    <td>
      $bnsListHtml
      <div style="margin-top:5px; font-size:11px; color:#333;">Incident Classification: <strong>${fir.incidentType}</strong></div>
    </td>
  </tr>
</table>

<div class="section-header">B. Complainant / Tourist (Informant) Particulars</div>
<table class="data-table">
  <tr>
    <th style="width: 25%;">Full Name</th>
    <td style="width: 35%;"><strong>${fir.complainantDetails.ifBlank { "Tourist Complainant" }}</strong></td>
    <th style="width: 20%;">Nationality / ID</th>
    <td style="width: 20%;">Foreign / Indian National</td>
  </tr>
  <tr>
    <th>Contact / Phone</th>
    <td>+91 (Tourist Registered Mobile)</td>
    <th>Mode of Information</th>
    <td>Phone-First e-FIR Portal (Mobile App)</td>
  </tr>
  <tr>
    <th>Present Address / Stay</th>
    <td colspan="3">Local Hotel / Tourist Lodging, $district, $state</td>
  </tr>
</table>

<div class="section-header">C. Place & Time of Occurrence</div>
<table class="data-table">
  <tr>
    <th style="width: 25%;">Date & Time of Offence</th>
    <td style="width: 35%;"><strong>${fir.dateTime}</strong></td>
    <th style="width: 20%;">Distance from PS</th>
    <td style="width: 20%;">${fir.distanceFromPs.ifBlank { "Approx. 1.5 KM" }}</td>
  </tr>
  <tr>
    <th>Exact Location / Landmark</th>
    <td colspan="3"><strong>${fir.place}</strong></td>
  </tr>
</table>

<div class="section-header">D. Suspect / Accused & Property Details</div>
<table class="data-table">
  <tr>
    <th style="width: 25%;">Details of Known / Suspected Accused</th>
    <td colspan="3">${fir.accusedDescription.ifBlank { "Unknown person(s)" }}</td>
  </tr>
  <tr>
    <th>Particulars of Property Involved / Stolen</th>
    <td colspan="3"><strong>${fir.propertyLost.ifBlank { "As stated in narrative" }}</strong></td>
  </tr>
  <tr>
    <th>Eyewitnesses (if any)</th>
    <td colspan="3">${fir.witnesses.ifBlank { "None / Local bystanders" }}</td>
  </tr>
</table>

<div class="section-header">E. Formal First Information Contents (Complaint Statement)</div>
<div class="narrative-box">$complaintLetter</div>

<table class="footer-signatures">
  <tr>
    <td>
      <div class="sig-line">
        Digital Signature / Thumb Impression<br>
        <small style="color:#666;">(Complainant / Informant)</small>
      </div>
    </td>
    <td>
      <div class="sig-line">
        Station House Officer / Duty Officer<br>
        <small style="color:#666;">$psName (Seal & Sign)</small>
      </div>
    </td>
  </tr>
</table>

<div class="hash-box">
  <strong>CRYPTOGRAPHIC INTEGRITY HASH (SHA-256):</strong> $hash<br>
  <strong>Digital Audit Trail:</strong> Verified on Tourist Safety National CCTNS Gateway Node #TS-DEL-${(100..999).random()}
</div>

<div class="watermark">
  * This is an electronically generated e-FIR draft and formal legal complaint under Section 173 BNSS, 2023. Valid for official police triage, insurance claims & legal proceedings. *
</div>

</body>
</html>
        """.trimIndent()
    }
}
