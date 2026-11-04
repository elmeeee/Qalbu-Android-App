package app.kamy.saatApp.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisposableEmailValidatorTest {

    @Test
    fun isDisposable_blocksGetnadaAndRelatedDomains() {
        assertTrue(DisposableEmailValidator.isDisposable("test@getnada.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@abunawa.com"))
        assertTrue(DisposableEmailValidator.isDisposable("temp@givmail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("inbox@inboxbear.com"))
        assertTrue(DisposableEmailValidator.isDisposable("anon@maildrop.cc"))
        assertTrue(DisposableEmailValidator.isDisposable("vip@nada.ltd"))
        assertTrue(DisposableEmailValidator.isDisposable("vip@nada.email"))
        assertTrue(DisposableEmailValidator.isDisposable("test@tempr.email"))
        assertTrue(DisposableEmailValidator.isDisposable("hello@tmail.ws"))
    }

    @Test
    fun isDisposable_blocksCommonDisposableDomains() {
        assertTrue(DisposableEmailValidator.isDisposable("user@tempmail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@temp-mail.org"))
        assertTrue(DisposableEmailValidator.isDisposable("user@10minutemail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@guerrillamail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@mailinator.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@yopmail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@throwawaymail.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@fakeinbox.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@dispostable.com"))
        assertTrue(DisposableEmailValidator.isDisposable("user@trashmail.com"))
    }

    @Test
    fun isDisposable_allowsLegitimateEmails() {
        assertFalse(DisposableEmailValidator.isDisposable("user@gmail.com"))
        assertFalse(DisposableEmailValidator.isDisposable("hello@elmee.my"))
        assertFalse(DisposableEmailValidator.isDisposable("john.doe@yahoo.com"))
        assertFalse(DisposableEmailValidator.isDisposable("worker@outlook.com"))
        assertFalse(DisposableEmailValidator.isDisposable("student@ui.ac.id"))
        assertFalse(DisposableEmailValidator.isDisposable("user@icloud.com"))
    }

    @Test
    fun isDisposable_handlesInvalidInputsGracefully() {
        assertFalse(DisposableEmailValidator.isDisposable(""))
        assertFalse(DisposableEmailValidator.isDisposable("no-at-symbol"))
        assertFalse(DisposableEmailValidator.isDisposable("@nodomain"))
    }
}

