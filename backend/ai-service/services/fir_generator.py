from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from typing import Dict, Any
import datetime

def generate_fir_pdf(fir_data: Dict[str, Any], output_path: str):
    doc = SimpleDocTemplate(output_path, pagesize=A4)
    styles = getSampleStyleSheet()
    
    # Custom styles
    title_style = ParagraphStyle(
        'TitleStyle', 
        parent=styles['Heading1'],
        alignment=1, # Center
        spaceAfter=20
    )
    heading_style = ParagraphStyle(
        'HeadingStyle', 
        parent=styles['Heading3'],
        spaceAfter=10,
        spaceBefore=10
    )
    body_style = styles['Normal']
    
    story = []
    
    # Header
    story.append(Paragraph("FIRST INFORMATION REPORT (e-FIR)", title_style))
    story.append(Paragraph("(Under Section 173 of Bharatiya Nagarik Suraksha Sanhita)", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    # Generated Date & Info
    gen_date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    story.append(Paragraph(f"<b>Generated On:</b> {gen_date}", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    # FIR Details
    story.append(Paragraph("1. Incident Details", heading_style))
    story.append(Paragraph(f"<b>Incident Type:</b> {fir_data.get('incident_type', 'N/A')}", body_style))
    story.append(Paragraph(f"<b>Date/Time of Occurrence:</b> {fir_data.get('date_time_of_occurrence', 'N/A')}", body_style))
    story.append(Paragraph(f"<b>Place of Occurrence:</b> {fir_data.get('place_of_occurrence', 'N/A')}", body_style))
    story.append(Paragraph(f"<b>Applicable BNS Sections:</b> {', '.join(fir_data.get('bns_sections', []))}", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("2. Complainant Details", heading_style))
    story.append(Paragraph(f"{fir_data.get('complainant_details', 'N/A')}", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("3. Accused & Property Details", heading_style))
    story.append(Paragraph(f"<b>Description of Accused:</b> {fir_data.get('description_of_accused', 'N/A')}", body_style))
    story.append(Paragraph(f"<b>Property Lost/Stolen:</b> {fir_data.get('property_lost', 'N/A')}", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("4. Incident Narrative", heading_style))
    story.append(Paragraph(f"{fir_data.get('narrative', 'N/A')}", body_style))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("5. Witnesses (if any)", heading_style))
    story.append(Paragraph(f"{fir_data.get('witnesses', 'N/A')}", body_style))
    story.append(Spacer(1, 0.5*inch))
    
    # Signatures
    story.append(Paragraph("_______________________                                     _______________________", body_style))
    story.append(Paragraph("Signature of Complainant                                     Signature of Officer in Charge", body_style))
    
    doc.build(story)
