package com.fathzer.jchess.lichess;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
class Opening {
	@JsonProperty("opening")
	private String name;
	private int count;
	private List<Move> moves;
}
