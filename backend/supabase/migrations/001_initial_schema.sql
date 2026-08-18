-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Updated at function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Profiles table
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    nationality TEXT,
    gender TEXT CHECK (gender IN ('male', 'female', 'other')),
    phone TEXT,
    email TEXT,
    passport_number TEXT,
    visa_number TEXT,
    profile_photo_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Emergency Contacts
CREATE TABLE emergency_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    relationship TEXT,
    country_code TEXT NOT NULL DEFAULT '+91',
    resident_address TEXT,
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Documents (E-Profile)
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    doc_type TEXT NOT NULL CHECK (doc_type IN ('passport', 'visa', 'flight_ticket', 'hotel_booking', 'travel_insurance', 'id_card', 'other')),
    doc_number TEXT,
    file_url TEXT NOT NULL,
    expiry_date DATE,
    verification_status TEXT DEFAULT 'pending' CHECK (verification_status IN ('pending', 'verified', 'rejected')),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Incidents
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES profiles(id) NOT NULL,
    incident_type TEXT NOT NULL CHECK (incident_type IN ('theft', 'snatching', 'harassment', 'accident', 'lost_item', 'fraud', 'assault', 'other')),
    description TEXT,
    ai_structured_fir TEXT,
    location GEOGRAPHY(POINT, 4326),
    address TEXT,
    state TEXT,
    district TEXT,
    police_station TEXT,
    status TEXT DEFAULT 'draft' CHECK (status IN ('draft', 'submitted', 'acknowledged', 'resolved')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_incidents_updated_at
    BEFORE UPDATE ON incidents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Incident Evidence
CREATE TABLE incident_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID REFERENCES incidents(id) ON DELETE CASCADE NOT NULL,
    file_url TEXT NOT NULL,
    file_type TEXT CHECK (file_type IN ('photo', 'video', 'audio')),
    ai_analysis JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- SOS Alerts
CREATE TABLE sos_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) NOT NULL,
    location GEOGRAPHY(POINT, 4326),
    trigger_type TEXT NOT NULL CHECK (trigger_type IN ('manual', 'scream_detected', 'inactivity', 'geofence_breach')),
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'resolved', 'false_alarm')),
    resolved_at TIMESTAMPTZ,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Geofence Zones
CREATE TABLE geofence_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    zone_type TEXT NOT NULL CHECK (zone_type IN ('danger', 'restricted', 'border', 'monument', 'natural_hazard', 'safe_zone')),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    radius DOUBLE PRECISION NOT NULL DEFAULT 500, -- meters
    description TEXT,
    alert_message TEXT,
    severity TEXT DEFAULT 'info' CHECK (severity IN ('info', 'warning', 'danger')),
    state TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Disaster Alerts (from SACHET)
CREATE TABLE disaster_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cap_identifier TEXT UNIQUE,
    hazard_type TEXT NOT NULL,
    severity TEXT CHECK (severity IN ('minor', 'moderate', 'severe', 'extreme')),
    urgency TEXT,
    headline TEXT,
    description TEXT,
    instructions TEXT,
    affected_states TEXT[] DEFAULT '{}',
    source TEXT DEFAULT 'SACHET',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Tourist Locations (for map/social)
CREATE TABLE tourist_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL UNIQUE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    is_visible BOOLEAN DEFAULT true,
    last_updated TIMESTAMPTZ DEFAULT NOW()
);

-- Chat Messages
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE NOT NULL,
    session_id UUID DEFAULT gen_random_uuid(),
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_incidents_reporter_id ON incidents(reporter_id);
CREATE INDEX idx_incidents_location ON incidents USING GIST (location);
CREATE INDEX idx_sos_alerts_user_id ON sos_alerts(user_id);
CREATE INDEX idx_sos_alerts_status ON sos_alerts(status);
CREATE INDEX idx_geofence_zones_active ON geofence_zones(is_active);
CREATE INDEX idx_chat_messages_user_id ON chat_messages(user_id);
CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id);

-- Enable RLS
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE emergency_contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE incidents ENABLE ROW LEVEL SECURITY;
ALTER TABLE incident_evidence ENABLE ROW LEVEL SECURITY;
ALTER TABLE sos_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE geofence_zones ENABLE ROW LEVEL SECURITY;
ALTER TABLE disaster_alerts ENABLE ROW LEVEL SECURITY;
ALTER TABLE tourist_locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;

-- RLS Policies

-- Profiles
CREATE POLICY "Users can read own profile" ON profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can insert own profile" ON profiles FOR INSERT WITH CHECK (auth.uid() = id);
CREATE POLICY "Users can update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);

-- Emergency Contacts
CREATE POLICY "Users can view own contacts" ON emergency_contacts FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can create own contacts" ON emergency_contacts FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own contacts" ON emergency_contacts FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own contacts" ON emergency_contacts FOR DELETE USING (auth.uid() = user_id);

-- Documents
CREATE POLICY "Users can view own documents" ON documents FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can create own documents" ON documents FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own documents" ON documents FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Users can delete own documents" ON documents FOR DELETE USING (auth.uid() = user_id);

-- Incidents
CREATE POLICY "Users can view own incidents" ON incidents FOR SELECT USING (auth.uid() = reporter_id);
CREATE POLICY "Users can create own incidents" ON incidents FOR INSERT WITH CHECK (auth.uid() = reporter_id);
CREATE POLICY "Users can update own incidents" ON incidents FOR UPDATE USING (auth.uid() = reporter_id);

-- Incident Evidence
CREATE POLICY "Users can view own incident evidence" ON incident_evidence FOR SELECT USING (
    EXISTS (SELECT 1 FROM incidents WHERE incidents.id = incident_evidence.incident_id AND incidents.reporter_id = auth.uid())
);
CREATE POLICY "Users can create own incident evidence" ON incident_evidence FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM incidents WHERE incidents.id = incident_evidence.incident_id AND incidents.reporter_id = auth.uid())
);
CREATE POLICY "Users can delete own incident evidence" ON incident_evidence FOR DELETE USING (
    EXISTS (SELECT 1 FROM incidents WHERE incidents.id = incident_evidence.incident_id AND incidents.reporter_id = auth.uid())
);

-- SOS Alerts
CREATE POLICY "Users can view own sos alerts" ON sos_alerts FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can create own sos alerts" ON sos_alerts FOR INSERT WITH CHECK (auth.uid() = user_id);
-- Service role policies omitted, they have superuser bypass

-- Geofence Zones
CREATE POLICY "Anyone can read geofence zones" ON geofence_zones FOR SELECT USING (true);

-- Disaster Alerts
CREATE POLICY "Anyone can read disaster alerts" ON disaster_alerts FOR SELECT USING (true);

-- Tourist Locations
CREATE POLICY "Users can view visible tourist locations" ON tourist_locations FOR SELECT USING (is_visible = true OR auth.uid() = user_id);
CREATE POLICY "Users can insert own location" ON tourist_locations FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can update own location" ON tourist_locations FOR UPDATE USING (auth.uid() = user_id);

-- Chat Messages
CREATE POLICY "Users can view own chat messages" ON chat_messages FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can insert own chat messages" ON chat_messages FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete own chat messages" ON chat_messages FOR DELETE USING (auth.uid() = user_id);
