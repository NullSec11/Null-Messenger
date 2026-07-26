package com.nullmessenger


import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth


val supabase: SupabaseClient = createSupabaseClient(
supabaseUrl = "https://ktdwlztlehvpnjwjwbzj.supabase.co",
supabaseKey = "sb_publishable_UQtIjulNbc86LK_3eTpRlg_ld3WuoDM"
) {
install(Auth)
}

