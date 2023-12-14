package it.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaimsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents the extractors chain. When the token is generated all registered extractors will be
 * invoked by adding information to the token.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class FjwtClaimsExtractorChain {

    /**
     * Extractors chain *
     */
    private final List<FjwtClaimsExtractor> fjwtClaimsExtractors;

    /**
     * Invokes the extractors chain adding information to the token and returns a map containing all
     * the extracted information
     *
     * @param source source object
     * @return a map containing all the extracted information
     */
    public Claims getClaims(UserDetails source) {

        DefaultClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();

        log.debug(
                "found [{}] extractors in chain",
                Objects.nonNull(fjwtClaimsExtractors) ? fjwtClaimsExtractors.size() : 0);

        if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
            fjwtClaimsExtractors.forEach(
                    ce -> {
                        log.info("retrieving claims from user using [{}]", ce.getClass().getName());
                        ce.getClaims(source, claimsBuilder);
                    });
        }
        return claimsBuilder.build();
    }

    /**
     * Invokes the extractors chain adding information to the user
     *
     * @param source source map
     * @param dest   destination user
     */
    public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {

        log.debug(
                "found [{}] extractors in chain",
                Objects.nonNull(fjwtClaimsExtractors) ? fjwtClaimsExtractors.size() : 0);

        if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
            fjwtClaimsExtractors.forEach(
                    ce -> {
                        log.info("retrieving claims from token using [{}]", ce.getClass().getName());
                        ce.addData(source, dest);
                    });
        }
    }
}
