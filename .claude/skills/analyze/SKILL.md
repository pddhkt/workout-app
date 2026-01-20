---
name: analyze
description: Market research and competitive analysis for feature planning. Researches competitors, industry trends, and best practices to inform development priorities.
allowed-tools:
  - Task
  - Bash
  - Read
  - WebFetch
  - WebSearch
  - AskUserQuestion
  - TodoWrite
---

# Analyze Skill

Conduct market research and competitive analysis to inform feature development. This skill helps prioritize features based on market demand, competitor offerings, and industry trends.

## Usage

```bash
/analyze "user authentication"                    # Research a feature area
/analyze --competitors                            # Analyze competitor features
/analyze --trends "SaaS project management"       # Research industry trends
/analyze --best-practices "API design"            # Find best practices
```

**Modes:**
- Default: Full analysis combining all modes
- `--competitors`: Focus on competitor feature comparison
- `--trends`: Focus on industry trends and emerging patterns
- `--best-practices`: Focus on implementation best practices

## Workflow

```
/analyze "feature area"
    ↓
1. Gather context from codebase
    ↓
2. Research competitors (if applicable)
    ↓
3. Identify industry trends
    ↓
4. Find best practices
    ↓
5. Generate prioritized recommendations
    ↓
6. User selects recommendation
    ↓
7. Transition to /phase-dev
```

## Phase 1: Context Gathering

Understand the current state:

```bash
# Read project description
cat README.md 2>/dev/null | head -50

# Check existing features
cat package.json 2>/dev/null | jq '.name, .description'

# Find related code
grep -r "auth\|login\|user" src/ --include="*.ts" -l 2>/dev/null | head -10
```

## Phase 2: Competitor Research

Research similar products in the space:

```
Use WebSearch to find:
- Top competitors in the space
- Feature comparison matrices
- User reviews mentioning missing features
- Industry reports

Example queries:
- "best [product category] tools 2024 comparison"
- "[competitor name] features list"
- "[product category] what users want"
```

### Competitor Analysis Template

```
┌────────────────────────────────────────────────────────────────────┐
│ COMPETITOR ANALYSIS: Authentication                                 │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ Feature                  │ Us │ Comp A │ Comp B │ Comp C │         │
│ ─────────────────────────┼────┼────────┼────────┼────────┤         │
│ Email/Password           │ ✓  │   ✓    │   ✓    │   ✓    │         │
│ OAuth (Google)           │ ✗  │   ✓    │   ✓    │   ✓    │         │
│ OAuth (GitHub)           │ ✗  │   ✓    │   ✗    │   ✓    │         │
│ Magic Link               │ ✗  │   ✓    │   ✓    │   ✗    │         │
│ 2FA/MFA                  │ ✗  │   ✓    │   ✓    │   ✓    │         │
│ SSO (SAML)               │ ✗  │   ✗    │   ✓    │   ✓    │         │
│ Passwordless             │ ✗  │   ✓    │   ✗    │   ✓    │         │
│                                                                     │
│ GAPS IDENTIFIED:                                                    │
│ • OAuth (High priority - all competitors have it)                   │
│ • 2FA/MFA (High priority - security expectation)                    │
│ • Magic Link (Medium priority - differentiator)                     │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
```

## Phase 3: Industry Trends

Identify emerging trends and patterns:

```
Use WebSearch for:
- "[industry] trends 2024"
- "emerging [feature area] technologies"
- "[feature area] future developments"
```

### Trends Output

```
┌────────────────────────────────────────────────────────────────────┐
│ INDUSTRY TRENDS: Authentication                                     │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 1. PASSWORDLESS AUTHENTICATION                          [RISING]    │
│    → WebAuthn/FIDO2 adoption growing 40% YoY                        │
│    → Major platforms (Google, Microsoft) pushing adoption           │
│    → User preference: 73% prefer passwordless when available        │
│                                                                     │
│ 2. ZERO-TRUST SECURITY                                  [RISING]    │
│    → Continuous verification beyond initial login                   │
│    → Session-based risk assessment                                  │
│    → Enterprise requirement for many buyers                         │
│                                                                     │
│ 3. SOCIAL LOGIN CONSOLIDATION                          [STABLE]     │
│    → Google and Apple dominant (85% of social logins)               │
│    → Facebook/Twitter declining in B2B contexts                     │
│                                                                     │
│ 4. BIOMETRIC AUTHENTICATION                            [EMERGING]   │
│    → Mobile-first apps leading adoption                             │
│    → Face ID/Touch ID integration                                   │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
```

## Phase 4: Best Practices

Research implementation best practices:

```
Use WebSearch for:
- "[feature] best practices"
- "how to implement [feature] securely"
- "[framework] [feature] tutorial"
```

### Best Practices Output

```
┌────────────────────────────────────────────────────────────────────┐
│ BEST PRACTICES: Authentication                                      │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ SECURITY                                                            │
│ • Use bcrypt/argon2 for password hashing (cost factor 12+)          │
│ • Implement rate limiting on auth endpoints                         │
│ • Use short-lived JWTs (15 min) with refresh tokens                 │
│ • Store refresh tokens server-side (not in localStorage)            │
│ • Implement CSRF protection for session-based auth                  │
│                                                                     │
│ USER EXPERIENCE                                                     │
│ • Show password strength meter                                      │
│ • Implement "remember me" with longer session                       │
│ • Clear error messages (not "invalid credentials")                  │
│ • Support password managers (autocomplete attributes)               │
│                                                                     │
│ COMPLIANCE                                                          │
│ • GDPR: Allow account deletion                                      │
│ • SOC2: Audit logging for auth events                               │
│ • HIPAA: Session timeout requirements                               │
│                                                                     │
│ RECOMMENDED LIBRARIES                                               │
│ • NextAuth.js (Next.js projects)                                    │
│ • Passport.js (Express/Node.js)                                     │
│ • Lucia (modern, lightweight)                                       │
│ • Clerk/Auth0 (managed solutions)                                   │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
```

## Phase 5: Generate Recommendations

Synthesize research into actionable recommendations:

```
=== ANALYSIS COMPLETE: Authentication ===

EXECUTIVE SUMMARY
-----------------
Based on competitor analysis, industry trends, and best practices,
here are prioritized recommendations for your authentication system:

RECOMMENDED FEATURES (Priority Order):

┌────────────────────────────────────────────────────────────────────┐
│ P1: HIGH PRIORITY (Must-Have)                                       │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 1. OAuth with Google                                                │
│    Impact: HIGH | Effort: M | Competitors: All have it              │
│    → Table stakes for modern apps                                   │
│    → 40% of users prefer social login                               │
│                                                                     │
│ 2. Two-Factor Authentication                                        │
│    Impact: HIGH | Effort: M | Competitors: 3/3 have it              │
│    → Security expectation for business users                        │
│    → Required for enterprise sales                                  │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ P2: MEDIUM PRIORITY (Should-Have)                                   │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 3. Magic Link Login                                                 │
│    Impact: MEDIUM | Effort: S | Differentiator                      │
│    → Growing user preference for passwordless                       │
│    → Quick win with existing email infrastructure                   │
│                                                                     │
│ 4. Session Management UI                                            │
│    Impact: MEDIUM | Effort: S | Best Practice                       │
│    → "View active sessions" and "logout all"                        │
│    → Security best practice, SOC2 requirement                       │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ P3: FUTURE CONSIDERATION                                            │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 5. Passkey/WebAuthn Support                                         │
│    Impact: MEDIUM | Effort: L | Emerging Trend                      │
│    → Industry moving toward passwordless                            │
│    → Consider after basic auth is solid                             │
│                                                                     │
│ 6. SSO/SAML for Enterprise                                          │
│    Impact: HIGH (for enterprise) | Effort: XL | Comp advantage      │
│    → Required for large enterprise customers                        │
│    → Consider when targeting enterprise segment                     │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘

=== END ANALYSIS ===

Select a recommendation to develop (1-6), or 'custom' for your own idea.
```

## Phase 6: User Selection

Present options to user:

```
Based on this analysis, which feature would you like to develop?

Options:
1. OAuth with Google [HIGH/M - Recommended]
2. Two-Factor Authentication [HIGH/M]
3. Magic Link Login [MEDIUM/S - Quick Win]
4. Session Management UI [MEDIUM/S]
5. Passkey/WebAuthn Support [MEDIUM/L]
6. SSO/SAML for Enterprise [HIGH/XL]
Other: Custom feature idea
```

## Phase 7: Transition to Phase Development

Once user selects:

```
User selected: "1. OAuth with Google"

Transitioning to /phase-dev...

Phase Description:
"Add OAuth authentication with Google. Users should be able to sign
in with their Google account, with automatic account linking if email
matches existing account. Include proper error handling for denied
permissions and token refresh."

Acceptance Criteria (from research):
- Users can sign in with Google in under 3 clicks
- Existing users with matching email are linked
- New users get account created automatically
- Token refresh handled transparently
- Proper error messages for denied permissions

Running /phase-dev...
```

## Research Sources

The skill uses these research approaches:

| Source | Method | Use Case |
|--------|--------|----------|
| Web Search | `WebSearch` tool | Competitor lists, trends |
| Documentation | `WebFetch` tool | Best practices, tutorials |
| GitHub | `WebFetch` tool | Popular implementations |
| Codebase | `Read`, `Grep` | Current state analysis |

## Example Session

```bash
# User starts analysis
/analyze "user authentication"

# Claude gathers context
# → Reads README, package.json
# → Checks existing auth code

# Claude researches competitors
# → WebSearch for auth comparisons
# → Builds feature matrix

# Claude identifies trends
# → WebSearch for auth trends 2024
# → Notes passwordless rising

# Claude finds best practices
# → WebSearch for auth best practices
# → Notes security requirements

# Claude presents recommendations
# === ANALYSIS COMPLETE ===
# [Recommendations presented]
# === END ANALYSIS ===

# User: "Let's do OAuth with Google"

# Claude transitions to /phase-dev
# → Creates tasks for OAuth implementation
```

## Error Handling

| Scenario | Action |
|----------|--------|
| Web search fails | Use cached/general knowledge |
| No competitors found | Focus on best practices |
| Feature area unclear | Ask for clarification |
| Analysis too broad | Suggest narrowing scope |

## Related Skills

| Skill | Relationship |
|-------|--------------|
| `/phase-dev` | Creates tasks from selected recommendation |
| `/ideation` | Complements with internal improvement ideas |
| `/project` | Provides project context |
| `/task-dev` | Execute created tasks |
