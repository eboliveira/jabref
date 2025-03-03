package org.jabref.logic.integrity;

import org.apache.commons.lang3.StringUtils;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.strings.StringUtil;
import org.jabref.preferences.JabRefPreferences;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;


public class EditionChecker implements ValueChecker {


    private static final Predicate<String> FIRST_LETTER_CAPITALIZED = Pattern.compile("^[A-Z]").asPredicate();
    private static final Predicate<String> ONLY_NUMERALS_OR_LITERALS = Pattern.compile("^([0-9]+|[^0-9].+)$")
            .asPredicate();
    private static final String FIRST_EDITION = "1";

    private final BibDatabaseContext bibDatabaseContextEdition;

    private final JabRefPreferences prefs = JabRefPreferences.getInstance();


    public EditionChecker(BibDatabaseContext bibDatabaseContext) {
        this.bibDatabaseContextEdition = Objects.requireNonNull(bibDatabaseContext);
    }

    /**
     * Checks, if field contains only an integer or a literal (biblatex mode)
     * Checks, if the first letter is capitalized (BibTeX mode)
     * biblatex package documentation:
     * The edition of a printed publication. This must be an integer, not an ordinal.
     * It is also possible to give the edition as a literal string, for example "Third, revised and expanded edition".
     * Official BibTeX specification:
     * The edition of a book-for example, "Second".
     * This should be an ordinal, and should have the first letter capitalized.
     */
    @Override
    public Optional<String> checkValue(String value) {
        if (StringUtil.isBlank(value)) {
            return Optional.empty();
        }

        if (value.equals(FIRST_EDITION)) {
            return Optional.of(Localization.lang("edition of book reported as just 1"));
        }

        //biblatex
        if (bibDatabaseContextEdition.isBiblatexMode() && !ONLY_NUMERALS_OR_LITERALS.test(value.trim())) {
            return Optional.of(Localization.lang("should contain an integer or a literal"));
        }

        //BibTeX
        if (!bibDatabaseContextEdition.isBiblatexMode()) {
            if(StringUtils.isNumeric(value.trim())){
                if(!prefs.getBoolean(JabRefPreferences.ALLOW_EDITION_INTEGER)){
                    return Optional.of(Localization.lang("should be a String but received a Integer"));
                }
            }else{
                if(!FIRST_LETTER_CAPITALIZED.test(value.trim())){
                    return Optional.of(Localization.lang("should have the first letter capitalized"));
                }
            }
        }

        return Optional.empty();
    }
}
