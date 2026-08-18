import feedparser
import re
from typing import List, Dict, Any

SACHET_RSS_URL = "https://sachet.ndma.gov.in/cap/rss"

def fetch_and_parse_sachet_alerts() -> List[Dict[str, Any]]:
    """
    Fetches CAP alerts from SACHET NDMA RSS feed and parses them.
    Fallback to checking WMO alert hub could be added here.
    """
    alerts = []
    try:
        feed = feedparser.parse(SACHET_RSS_URL)
        
        for entry in feed.entries:
            # Parse CAP elements from the entry if available
            # SACHET puts some info in summary, some in specific CAP tags
            identifier = entry.get('id', '')
            headline = entry.get('title', '')
            description = entry.get('summary', '')
            
            # Simple extraction from title/summary if CAP tags aren't standard
            hazard_type = _extract_hazard(headline)
            severity = _extract_severity(description)
            
            alerts.append({
                "cap_identifier": identifier,
                "hazard_type": hazard_type,
                "severity": severity,
                "urgency": "Immediate", # Default or extract
                "headline": headline,
                "description": description,
                "instructions": "Follow local authorities' instructions.",
                "affected_states": [], # Could extract from geography elements
                "source": "SACHET",
            })
    except Exception as e:
        print(f"Error fetching SACHET alerts: {e}")
        
    return alerts

def _extract_hazard(text: str) -> str:
    text = text.lower()
    if 'cyclone' in text: return 'Cyclone'
    if 'flood' in text: return 'Flood'
    if 'earthquake' in text: return 'Earthquake'
    if 'tsunami' in text: return 'Tsunami'
    if 'heat' in text: return 'Heat Wave'
    return 'Other'

def _extract_severity(text: str) -> str:
    text = text.lower()
    if 'extreme' in text or 'red' in text: return 'extreme'
    if 'severe' in text or 'orange' in text: return 'severe'
    if 'moderate' in text or 'yellow' in text: return 'moderate'
    return 'minor'
