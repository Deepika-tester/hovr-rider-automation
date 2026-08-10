# HOVR Cucumber/Gherkin Test Suite

Comprehensive BDD test cases for the HOVR rideshare platform, covering the Rider App, Driver App, and Backend API services.

## Directory Structure

```
features/
  rider/
    rider_registration.feature    # Signup, phone verification, OTP, profile setup
    rider_booking.feature         # Search destination, ride type selection, confirm ride
    rider_payment.feature         # Add/remove payment methods (Adyen), default card
    rider_trip.feature            # Ride tracking, driver arrival, in-trip, completion
    rider_rating.feature          # Rate driver, compliment, complaint, tip
    rider_referral.feature        # Referral code, share, redeem, QR scan, progress
    rider_profile.feature         # Edit profile, saved places, ride history, settings
    rider_promo.feature           # Apply promo code, wallet credits, campaign discounts
  driver/
    driver_registration.feature   # Signup, document upload, onboarding checklist
    driver_online.feature         # Go online/offline, location streaming, driver stream
    driver_ride_request.feature   # Receive dispatch offer, accept/decline, timeout
    driver_trip.feature           # Navigate to pickup, arrive, start trip, complete, rate
    driver_earnings.feature       # Daily/weekly earnings, trip details, 100% fare verify
    driver_membership.feature     # $20/month subscription, Adyen webview, manage/cancel
    driver_profile.feature        # Account, vehicle, documents, banking, settings, alerts
    driver_assignment.feature     # Recurring pre-scheduled routes (new feature spec)
  api/
    api_dispatch.feature          # Dispatch algorithm, geoindex matching, driver streams
    api_pricing.feature           # Fare calculation, breakdown, no surge, promo credits
    api_auth.feature              # OTP auth, token management, profile CRUD, referrals
```

## Source Codebase Mapping

These test cases were derived from analysis of the actual HOVR codebase:

| Feature Area | Source Code |
|-------------|------------|
| **Rider Auth** | `hovr-rider-app/lib/features/authentication/` (AuthBloc, AuthState, AuthEvent) |
| **Ride Booking** | `hovr-rider-app/lib/features/ride-booking/` (RideBookingBloc, FindRideRequest/Response) |
| **Rider Trip** | `hovr-rider-app/lib/features/trip-feature/` (TripBloc, TripStreamResponse, states) |
| **Rider Payment** | `hovr-rider-app/lib/features/payment/` (PaymentBloc, Adyen drop-in) |
| **Rider Referral** | `hovr-rider-app/lib/features/refer-and-earn/` (ReferralBloc) |
| **Rider Profile** | `hovr-rider-app/lib/features/home-view/` (saved places, settings, trips) |
| **Driver Auth** | `hovr-driver-app/lib/features/authentication-feature/` |
| **Driver Onboarding** | `hovr-driver-app/lib/features/driver-onboarding-feature/` (S3 upload, checklist) |
| **Driver Ride** | `hovr-driver-app/lib/features/driver-ride-feature/` (DriverStream, DispatchOffer) |
| **Driver Trip** | `hovr-driver-app/lib/features/trip-feature/` (navigate, arrive, start, complete) |
| **Driver Membership** | `hovr-driver-app/lib/features/membership-feature/` (GetPaymentURL, webview) |
| **Driver Earnings** | `hovr-driver-app/lib/features/home-view/.../earning/` |
| **Driver Profile** | `hovr-driver-app/lib/features/home-view/.../drawer-screens/` |
| **Driver Documents** | `hovr-driver-app/lib/features/documents-feature/` |
| **Assignments** | `engineering-brain/product-features/assignments-feature.md` (spec only) |
| **API Dispatch** | `ride-service/internal/v1/handler/` (Go, geoindex, Centrifuge realtime) |
| **API Auth** | `iam-service/internal/v1/handler/` (auth, driver, rider, referral, payment, wallet) |

## Tech Stack Reference

- **Mobile Apps**: Flutter/Dart (iOS + Android)
- **State Management**: BLoC pattern (flutter_bloc)
- **Backend Services**: Go (gRPC + REST)
- **Databases**: DynamoDB (primary), Redis (geoindex, cache)
- **Storage**: AWS S3 (document uploads)
- **Payment**: Adyen (drop-in for riders, webview for driver membership)
- **Real-time**: Centrifuge (WebSocket pub/sub for dispatch and trip streams)
- **Spatial**: Custom geoindex backed by Redis (driver location tracking)
- **SDK**: hovr_sdk (Dart protobuf-generated types shared between apps)
- **Notifications**: Custom notification router
- **Observability**: Sentry (error tracking)
- **Support**: Zendesk (rider help)

## API Endpoints (Service Hosts)

| Service | Production Host |
|---------|----------------|
| IAM     | `iam.api.prod.ridehovr.com` |
| Ride    | `ca.ride.api.prod.ridehovr.com` |
| Driver  | `ca.driver.api.prod.ridehovr.com` |

## Key Business Rules (Test Against These)

1. **100% Fare Retention**: Drivers keep 100% of every ride fare. Only cost is $20/month membership.
2. **No Surge Pricing**: HOVR does not apply surge multipliers. Fares are consistent regardless of demand.
3. **OTP Expiry**: OTP codes expire after 5 minutes.
4. **Membership Required**: Drivers must have an active $20/month membership to go online.
5. **Driver Rating >= 4.7**: Required for Assignment Program eligibility.
6. **Assignment 5-Minute Window**: Drivers must arrive within 5 minutes of the scheduled pickup time.
7. **Assignment Cancellation**: >= 24 hours notice required to avoid strikes.
8. **Strike System**: 1st strike = 14-day suspension, 2nd = 60-day, 3rd = permanent ban from Assignments.
9. **Supported Languages**: English and French (l10n).
10. **Active Marketplaces**: GTA (primary, ~2,769 drivers), Ottawa (secondary, ~274 drivers).

## Tags

Use tags to run specific subsets of tests:

| Tag | Description |
|-----|-------------|
| `@rider` | All rider app tests |
| `@driver` | All driver app tests |
| `@api` | All backend API tests |
| `@auth` | Authentication/registration tests |
| `@core` | Critical path / smoke tests |
| `@booking` | Ride booking flow tests |
| `@trip` | Trip lifecycle tests |
| `@payment` | Payment-related tests |
| `@rating` | Rating and feedback tests |
| `@referral` | Referral program tests |
| `@profile` | Profile and settings tests |
| `@promo` | Promotions and discounts |
| `@membership` | Driver membership tests |
| `@earnings` | Driver earnings tests |
| `@online` | Driver go online/offline tests |
| `@riderequest` | Ride request handling tests |
| `@dispatch` | Dispatch algorithm tests |
| `@assignment` | Recurring assignments (new feature) |
| `@backend` | Backend-only (no UI) tests |
| `@registration` | Registration/onboarding tests |
| `@new-feature` | Tests for features in specification (not yet built) |

### Example: Run only core rider tests

```bash
cucumber --tags "@rider and @core"
```

### Example: Run all registration tests across both apps

```bash
cucumber --tags "@registration"
```

### Example: Run everything except the not-yet-built assignment feature

```bash
cucumber --tags "not @new-feature"
```

## Scenario Statistics

| Category | Feature Files | Scenarios |
|----------|--------------|-----------|
| Rider App | 8 | ~85 |
| Driver App | 8 | ~95 |
| Backend API | 3 | ~55 |
| **Total** | **19** | **~235** |

## How to Use With Cucumber.io

1. **Install Cucumber** for your language of choice (Java, JavaScript, Ruby, Python, etc.)
2. **Copy the `features/` directory** into your Cucumber project
3. **Implement step definitions** that map Given/When/Then steps to your test automation code
4. **Connect to test environment**: Point tests at staging endpoints:
   - IAM: `iam.api.staging.ridehovr.com`
   - Ride: `ca.ride.api.staging.ridehovr.com`
   - Driver: `ca.driver.api.staging.ridehovr.com`
5. **Run**: `cucumber features/` or target specific files/tags

## Step Definition Hints

For UI tests (rider/driver apps), step definitions will need:
- **Flutter Driver** or **Patrol** for Flutter app automation
- **Appium** as an alternative cross-platform option
- Widget finders for HOVR-specific UI components

For API tests, step definitions will need:
- HTTP/gRPC client for the Go backend services
- Auth token management (OTP flow for test accounts)
- DynamoDB test data setup/teardown
- Redis state management for geoindex

## Notes

- The **driver_assignment.feature** tests a feature that is in specification phase (tagged `@new-feature`). Implement step definitions once the feature is built.
- All feature files include comments referencing the exact source code files they were derived from.
- The BLoC state machine transitions documented in trip features are the authoritative reference for the expected app behavior.
- Adyen payment integration tests may require Adyen test credentials and sandbox environment.

---

Generated by Tia (HOVR Senior Engineer) from analysis of the HOVR codebase (551K LOC across 48 repos).
