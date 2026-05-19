package com.bibli.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.bibli.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LoanDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(LoanDTO.class);
        LoanDTO loanDTO1 = new LoanDTO();
        loanDTO1.setId(1L);
        LoanDTO loanDTO2 = new LoanDTO();
        assertThat(loanDTO1).isNotEqualTo(loanDTO2);
        loanDTO2.setId(loanDTO1.getId());
        assertThat(loanDTO1).isEqualTo(loanDTO2);
        loanDTO2.setId(2L);
        assertThat(loanDTO1).isNotEqualTo(loanDTO2);
        loanDTO1.setId(null);
        assertThat(loanDTO1).isNotEqualTo(loanDTO2);
    }
}
