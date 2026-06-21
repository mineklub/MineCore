package dk.mineclub.minecore.api.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/** Response from GET /server/vote. Contains either a flat list or paginated data with meta. */
@Getter
@Setter
@ToString
@SuppressWarnings("unused")
public class GetVotesResponse {
    /** Present when withMeta=true. */
    @Nullable private List<MappedVote> data;

    /** Set internally when the API returns a plain array (withMeta=false). */
    @Nullable private List<MappedVote> votes;

    @Nullable private Pagination pagination;
    @Nullable private Filters filters;

    /** Returns the vote list regardless of whether withMeta was used. */
    public List<MappedVote> getAll() {
        return data != null ? data : votes;
    }

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Pagination {
        private int page;
        private int limit;
        private int total;
        private int totalPages;
    }

    @Getter
    @ToString
    @SuppressWarnings("unused")
    public static class Filters {
        private String status;
        private String sortBy;
        private String sortOrder;
        private String mcaccount;
        private String from;
        private String to;
    }
}
