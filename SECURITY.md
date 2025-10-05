# Security Setup Guide

## 🔒 Environment Variables Setup

### For Local Development:

1. **Copy environment template:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` file with your actual values:**
   ```bash
   # Database Configuration
   DB_URL=jdbc:mysql://localhost:3306/your_database
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   
   # JWT Configuration
   JWT_SECRET=your_base64_encoded_secret_key
   JWT_EXPIRATION=3600000
   ```

3. **Generate new JWT Secret Key (recommended):**
   ```java
   // Run this Java code to generate a secure key
   import java.util.Base64;
   import java.security.SecureRandom;
   
   SecureRandom random = new SecureRandom();
   byte[] key = new byte[32];
   random.nextBytes(key);
   String secretKey = Base64.getEncoder().encodeToString(key);
   System.out.println("JWT Secret Key: " + secretKey);
   ```

### For Production:

Set environment variables on your server:
```bash
export DB_URL=jdbc:mysql://prod-server:3306/sshop_prod
export DB_USERNAME=prod_user
export DB_PASSWORD=super_secure_password
export JWT_SECRET=your_production_jwt_secret
export JWT_EXPIRATION=86400000
```

## 🚀 Running the Application:

### Local Development:
```bash
# Using script (loads .env automatically)
./run.sh    # Linux/Mac
run.bat     # Windows

# Or manually with environment variables
java -jar target/sshop-0.0.1-SNAPSHOT.jar
```

### Production:
```bash
# With environment variables set
java -jar target/sshop-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

## ⚠️ Security Notes:

1. **Never commit `.env` files** - they are ignored by Git
2. **Use strong passwords** for production database
3. **Generate unique JWT secrets** for each environment
4. **Use HTTPS** in production
5. **Set `spring.jpa.hibernate.ddl-auto=validate`** in production