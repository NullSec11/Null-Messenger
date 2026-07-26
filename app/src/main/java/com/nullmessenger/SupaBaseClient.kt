package com.nullmessenger

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://ktdwlztlehvpnjwjwbzj.supabase.co",
    supabaseKey = "ТВОЙ_PUBLISHABLE_KEY"
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
    install(Storage)
}
