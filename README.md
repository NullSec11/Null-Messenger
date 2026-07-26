# Null Messenger

A dark, NullSec-styled Android messenger shell built with Jetpack Compose.

## What is included
- Login / sign-up screen
- Chats, contacts, profile, and settings tabs
- Supabase config plumbing
- GitHub Actions workflow for Android CI
- NullSec-style branding and launcher icon

## Backend setup
Set your Supabase project URL in `gradle.properties`:

```properties
supabaseUrl=https://your-project.supabase.co
```

The publishable key is already wired in. If you want to change it, update `supabaseAnonKey`.

## Build
This repository includes a `gradlew` helper script that will use Gradle if it is available, or download a local Gradle distribution on demand.
