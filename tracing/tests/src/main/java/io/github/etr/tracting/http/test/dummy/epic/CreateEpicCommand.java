package io.github.etr.tracting.http.test.dummy.epic;

import java.util.List;

public record CreateEpicCommand(String title, List<Long> todoIds) {
}
