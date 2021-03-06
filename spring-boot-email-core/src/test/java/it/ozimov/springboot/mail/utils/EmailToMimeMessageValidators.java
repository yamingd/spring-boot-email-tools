package it.ozimov.springboot.mail.utils;

import com.google.common.collect.ImmutableSet;
import it.ozimov.springboot.mail.model.Email;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

public class EmailToMimeMessageValidators {

    public static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    public static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    @Rule
    public final JUnitSoftAssertions assertions = new JUnitSoftAssertions();

    public void validateFrom(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> froms = asList(sentMessage.getFrom());
        assertThat(froms, hasSize(1)); // redundant with contains
        assertThat(froms, contains((Address) email.getFrom()));
    }

    public void validateReplyTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> replyTos = asList(sentMessage.getReplyTo());
        assertThat(replyTos, hasSize(1)); // redundant with contains
        assertThat(replyTos, contains((Address) email.getReplyTo()));
    }

    public void validateTo(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> tos = asList(sentMessage.getRecipients(TO));
        assertThat(tos.get(0), is((new ArrayList<>(email.getTo()).get(0))));
        assertThat(tos, everyItem(is(in(toAddress(email.getTo())))));
    }

    public void validateCc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> ccs = asList(sentMessage.getRecipients(CC));
        assertThat(ccs, everyItem(is(in(toAddress(email.getCc())))));
    }

    public void validateBcc(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        final List<Address> bccs = asList(sentMessage.getRecipients(BCC));
        assertThat(bccs, everyItem(is(in(toAddress(email.getBcc())))));
    }

    public void validateReceipt(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_RETURN_RECEIPT)[0], is(email.getReceiptTo().getAddress()));
    }

    public void validateDepositionNotification(Email email, MimeMessage sentMessage) throws MessagingException {
        assertThat(sentMessage.getHeader(HEADER_DEPOSITION_NOTIFICATION_TO)[0], is(email.getReceiptTo().getAddress()));
    }

    public void validateSubject(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getSubject(), is(email.getSubject()));
    }

    public void validateBody(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        assertThat(sentMessage.getContent(), is(email.getBody()));
    }

    public void validateCustomHeaders(final Email email, final MimeMessage sentMessage)
            throws MessagingException, IOException {
        Map<String, String> customHeaders = email.getCustomHeaders();
        List<Header> internetHeaders = (List<Header>)  Collections.list(sentMessage.getAllHeaders());
        List<String> headerKeys = internetHeaders.stream().map(Header::getName).collect(toList());

        assertions.assertThat(headerKeys)
                .as("Should contains all the headers keys provided at construction time")
                .containsAll(customHeaders.keySet());

        customHeaders.entrySet().stream()
                .forEach(entry -> {
                    try {
                        assertions.assertThat(sentMessage.getHeader(entry.getKey())).isNotNull().containsExactly(entry.getValue());
                    } catch (MessagingException e) {
                    }
                });
    }

    private static List<Address> toAddress(final Collection<InternetAddress> internetAddresses) {
        return internetAddresses.stream().map(internetAddress -> (Address) internetAddress).collect(toList());
    }

}
