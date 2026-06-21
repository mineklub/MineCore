package dk.mineclub.minecore.api.model;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
@Builder
public class GetVotesOptions {
    @Nullable private Integer page;
    @Nullable private Integer limit;
    @Nullable private VoteStatusQuery status;
    @Nullable private VoteSortByQuery sortBy;
    @Nullable private VoteSortOrderQuery order;
    @Nullable private String mcaccount;
    @Nullable private Date from;
    @Nullable private Date to;
    @Nullable private Boolean withMeta;
}
