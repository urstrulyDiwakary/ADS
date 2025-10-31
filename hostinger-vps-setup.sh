#!/bin/bash
# Complete Deployment Script for Hostinger VPS
# This script automates the entire deployment process

set -e  # Exit on any error

echo "════════════════════════════════════════════════════════════"
echo "  🚀 ADS Admin System - Hostinger VPS Deployment"
echo "════════════════════════════════════════════════════════════"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="ads-admin"
APP_VERSION="1.0.0"
APP_DIR="/opt/${APP_NAME}"
LOG_DIR="/var/log/ads"
ENV_FILE="/etc/${APP_NAME}/production.env"
SYSTEMD_SERVICE="/etc/systemd/system/${APP_NAME}.service"
DB_NAME="ads_production"
DB_USER="ads_user"

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "ℹ️  $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    print_error "Please run as root (use sudo)"
    exit 1
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 1: System Update & Package Installation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Updating system packages..."
apt update && apt upgrade -y
print_success "System updated"

print_info "Installing required packages..."
apt install -y openjdk-17-jdk postgresql postgresql-contrib nginx curl ufw
print_success "Packages installed"

# Verify Java installation
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    print_success "Java installed: $JAVA_VERSION"
else
    print_error "Java installation failed"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 2: Creating Application User & Directories"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Create application user
if id "$APP_NAME" &>/dev/null; then
    print_warning "User $APP_NAME already exists"
else
    useradd -r -m -s /bin/bash $APP_NAME
    print_success "Created user: $APP_NAME"
fi

# Create directories
print_info "Creating application directories..."
mkdir -p $APP_DIR
mkdir -p $LOG_DIR
mkdir -p /etc/$APP_NAME

# Set permissions
chown -R $APP_NAME:$APP_NAME $APP_DIR
chown -R $APP_NAME:$APP_NAME $LOG_DIR
chown -R $APP_NAME:$APP_NAME /etc/$APP_NAME
print_success "Directories created and permissions set"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 3: PostgreSQL Database Setup"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Start PostgreSQL
systemctl start postgresql
systemctl enable postgresql
print_success "PostgreSQL started and enabled"

# Generate secure database password
DB_PASSWORD=$(openssl rand -base64 32)

print_info "Creating database and user..."
sudo -u postgres psql << EOF
-- Drop existing database and user if they exist (for clean reinstall)
DROP DATABASE IF EXISTS $DB_NAME;
DROP USER IF EXISTS $DB_USER;

-- Create fresh database and user
CREATE DATABASE $DB_NAME;
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
ALTER USER $DB_USER CREATEDB;

-- Grant schema permissions
\c $DB_NAME
GRANT ALL ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $DB_USER;
EOF

print_success "Database created: $DB_NAME"
print_success "Database user created: $DB_USER"

# Save database credentials
echo "$DB_PASSWORD" > /etc/$APP_NAME/db_password.txt
chmod 600 /etc/$APP_NAME/db_password.txt
chown $APP_NAME:$APP_NAME /etc/$APP_NAME/db_password.txt

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 4: Environment Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Generate secure admin password
ADMIN_PASSWORD=$(openssl rand -base64 24)

print_info "Creating environment configuration..."
cat > $ENV_FILE << EOF
# ADS Admin System - Production Environment Configuration
# Auto-generated on $(date)

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/$DB_NAME
DATABASE_USERNAME=$DB_USER
DATABASE_PASSWORD=$DB_PASSWORD
DB_POOL_SIZE=10
DB_POOL_MIN=2

# Admin Account Configuration
ADMIN_DEFAULT_EMAIL=admin@yourdomain.com
ADMIN_DEFAULT_PASSWORD=$ADMIN_PASSWORD
ADMIN_DEFAULT_USERNAME=admin
ADMIN_DEFAULT_FULLNAME=System Administrator

# Application Configuration
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Security Configuration
COOKIE_SECURE=true
THYMELEAF_CACHE=true

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# Logging
LOG_LEVEL=INFO

# JVM Options
JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0
EOF

chmod 600 $ENV_FILE
chown $APP_NAME:$APP_NAME $ENV_FILE
print_success "Environment file created: $ENV_FILE"

# Save admin credentials
cat > /etc/$APP_NAME/admin_credentials.txt << EOF
Admin Login Credentials
=======================
Email: admin@yourdomain.com
Password: $ADMIN_PASSWORD

⚠️  IMPORTANT: Change these credentials immediately after first login!
⚠️  This file will be deleted after deployment.
EOF

chmod 600 /etc/$APP_NAME/admin_credentials.txt
print_success "Admin credentials saved"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 5: Systemd Service Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Creating systemd service..."
cat > $SYSTEMD_SERVICE << EOF
[Unit]
Description=ADS Admin System
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=$APP_NAME
Group=$APP_NAME
WorkingDirectory=$APP_DIR
EnvironmentFile=$ENV_FILE
ExecStart=/usr/bin/java \$JAVA_OPTS -Dspring.profiles.active=prod -jar $APP_DIR/app.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# Security settings
NoNewPrivileges=true
PrivateTmp=true

# Logging
SyslogIdentifier=$APP_NAME

[Install]
WantedBy=multi-user.target
EOF

print_success "Systemd service created"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Step 6: Firewall Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Configuring firewall..."
ufw --force enable
ufw allow 22/tcp    # SSH
ufw allow 80/tcp    # HTTP
ufw allow 443/tcp   # HTTPS
print_success "Firewall configured"

echo ""
print_warning "JAR file deployment required:"
print_info "Upload your JAR file to: $APP_DIR/app.jar"
print_info "Example: scp target/ads-admin-1.0.0.jar root@your-vps-ip:$APP_DIR/app.jar"
print_info "Then run: chown $APP_NAME:$APP_NAME $APP_DIR/app.jar"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  📋 Deployment Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
print_success "System setup completed successfully!"
echo ""
echo "📁 Application directory: $APP_DIR"
echo "📝 Log directory: $LOG_DIR"
echo "⚙️  Environment file: $ENV_FILE"
echo "🗄️  Database name: $DB_NAME"
echo "👤 Database user: $DB_USER"
echo ""
print_warning "IMPORTANT: Admin credentials saved in:"
echo "   /etc/$APP_NAME/admin_credentials.txt"
echo ""
print_warning "IMPORTANT: Database password saved in:"
echo "   /etc/$APP_NAME/db_password.txt"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  🚀 Next Steps"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Upload your JAR file:"
echo "   scp target/ads-admin-1.0.0.jar root@your-vps-ip:$APP_DIR/app.jar"
echo ""
echo "2. Set JAR file ownership:"
echo "   chown $APP_NAME:$APP_NAME $APP_DIR/app.jar"
echo ""
echo "3. Start the application:"
echo "   systemctl daemon-reload"
echo "   systemctl enable $APP_NAME"
echo "   systemctl start $APP_NAME"
echo ""
echo "4. Check status:"
echo "   systemctl status $APP_NAME"
echo "   journalctl -u $APP_NAME -f"
echo ""
echo "5. View admin credentials:"
echo "   cat /etc/$APP_NAME/admin_credentials.txt"
echo ""
echo "6. Configure Nginx (see nginx.conf in your project)"
echo ""
echo "7. Install SSL certificate:"
echo "   certbot --nginx -d yourdomain.com"
echo ""
echo "════════════════════════════════════════════════════════════"
print_success "Setup completed! Ready for application deployment."
echo "════════════════════════════════════════════════════════════"

