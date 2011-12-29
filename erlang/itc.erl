%% ``The contents of this file are subject to the Erlang Public License,
%% Version 1.1, (the "License"); you may not use this file except in
%% compliance with the License. You should have received a copy of the
%% Erlang Public License along with this software. If not, it can be
%% retrieved via the world wide web at http://www.erlang.org/.
%% 
%% Software distributed under the License is distributed on an "AS IS"
%% basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
%% the License for the specific language governing rights and limitations
%% under the License.
%% 
-module(itc).
-author("Paulo Sergio Almeida <psa@di.uminho.pt>").
-export([seed/0, event/1, join/2, fork/1, peek/1, leq/2]).
-export([len/1, str/1, encode/1, decode/1]).
-compile({inline, [{min,2}, {max,2}, {drop,2}, {lift,2}, {base,1},
     {height,1}]}).

seed() -> {1, 0}.

join({I1, E1}, {I2, E2}) -> {sum(I1,I2), join_ev(E1, E2)}.

fork({I, E}) ->
{I1, I2} = split(I),
{{I1, E}, {I2, E}}.

peek({I, E}) -> {{0, E}, {I, E}}.

event({I, E}) ->
{I,
case fill(I, E) of
 E -> {_, E1} = grow(I, E), E1;
 E1 -> E1
end
}.

leq({_, E1}, {_, E2}) -> leq_ev(E1, E2).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

leq_ev({N1, L1, R1}, {N2, L2, R2}) ->
N1 =< N2 andalso
leq_ev(lift(N1, L1), lift(N2, L2)) andalso
leq_ev(lift(N1, R1), lift(N2, R2));

leq_ev({N1, L1, R1}, N2) ->
N1 =< N2 andalso
leq_ev(lift(N1, L1), N2) andalso
leq_ev(lift(N1, R1), N2);

leq_ev(N1, {N2, _, _}) -> N1 =< N2;

leq_ev(N1, N2) -> N1 =< N2.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Normal form

norm_id({0, 0}) -> 0;
norm_id({1, 1}) -> 1;
norm_id(X) -> X.

norm_ev({N, M, M}) when is_integer(M) -> N + M;
norm_ev({N, L, R}) ->
M = min(base(L), base(R)),
{N + M, drop(M, L), drop(M, R)}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sum(0, X) -> X;
sum(X, 0) -> X;
sum({L1,R1}, {L2, R2}) -> norm_id({sum(L1, L2), sum(R1, R2)}).

split(0) -> {0, 0};
split(1) -> {{1, 0}, {0, 1}};
split({0, I}) -> {I1, I2} = split(I), {{0, I1}, {0, I2}};
split({I, 0}) -> {I1, I2} = split(I), {{I1, 0}, {I2, 0}};
split({I1, I2}) -> {{I1, 0}, {0, I2}}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

join_ev(E1={N1, _, _}, E2={N2, _, _}) when N1 > N2 -> join_ev(E2, E1);
join_ev({N1, L1, R1}, {N2, L2, R2}) when N1 =< N2 ->
D = N2 - N1,
norm_ev({N1, join_ev(L1, lift(D, L2)), join_ev(R1, lift(D, R2))});
join_ev(N1, {N2, L2, R2}) -> join_ev({N1, 0, 0}, {N2, L2, R2});
join_ev({N1, L1, R1}, N2) -> join_ev({N1, L1, R1}, {N2, 0, 0});
join_ev(N1, N2) -> max(N1, N2).

fill(0, E) -> E;
fill(1, E={_, _, _}) -> height(E);
fill(_, N) when is_integer(N) -> N;
fill({1, R}, {N, El, Er}) ->
Er1 = fill(R, Er),
D = max(height(El), base(Er1)),
norm_ev({N, D, Er1});
fill({L, 1}, {N, El, Er}) ->
El1 = fill(L, El),
D = max(height(Er), base(El1)),
norm_ev({N, El1, D});
fill({L, R}, {N, El, Er}) ->
norm_ev({N, fill(L, El), fill(R, Er)}).

grow(1, N) when is_integer(N)->
{0, N + 1};
grow({0, I}, {N, L, R}) ->
{H, E1} = grow(I, R),
{H + 1, {N, L, E1}};
grow({I, 0}, {N, L, R}) ->
{H, E1} = grow(I, L),
{H + 1, {N, E1, R}};
grow({Il, Ir}, {N, L, R}) ->
{Hl, El} = grow(Il, L),
{Hr, Er} = grow(Ir, R),
if
Hl < Hr -> {Hl + 1, {N, El, R}};
true ->    {Hr + 1, {N, L, Er}}
end;
grow(I, N) when is_integer(N)->
{H, E} = grow(I, {N, 0, 0}),
{H + 1000, E}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

height({N, L, R}) -> N + max(height(L), height(R));
height(N) -> N.

base({N, _, _}) -> N;
base(N) -> N.

lift(M, {N, L, R}) -> {N + M, L ,R};
lift(M, N) -> N + M.

drop(M, {N, L, R}) when M =< N -> {N - M, L ,R};
drop(M, N) when M =< N -> N - M.

max(X, Y) when X =< Y -> Y;
max(X, _) -> X.

min(X, Y) when X =< Y -> X;
min(_, Y) -> Y.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

encode({I, E}) -> << (enci(I))/bits, (ence(E))/bits >>.

decode(B) -> {I, BE} = deci(B), {E, <<>>} = dece(BE), {I, E}.

enci(0) -> <<0:2, 0:1>>;
enci(1) -> <<0:2, 1:1>>;
enci({0, I}) -> <<1:2, (enci(I))/bits>>;
enci({I, 0}) -> <<2:2, (enci(I))/bits>>;
enci({L, R}) -> <<3:2, (enci(L))/bits, (enci(R))/bits>>.

deci(<<0:2, 0:1, B/bits>>) -> {0, B};
deci(<<0:2, 1:1, B/bits>>) -> {1, B};
deci(<<1:2, B/bits>>) -> {I, B1} = deci(B), {{0, I}, B1};
deci(<<2:2, B/bits>>) -> {I, B1} = deci(B), {{I, 0}, B1};
deci(<<3:2, B/bits>>) -> {L, B1} = deci(B), {R, B2} = deci(B1), {{L, R}, B2}.

%ence({0, 0, R}) -> <<0:1, 0:2, (ence(R))/bits>>;
%ence({0, L, 0}) -> <<0:1, 1:2, (ence(L))/bits>>;
%ence({0, L, R}) -> <<0:1, 2:2, (ence(L))/bits, (ence(R))/bits>>;
%ence({N, L, R}) -> <<0:1, 3:2, (ence(N))/bits, (ence(L))/bits, (ence(R))/bits>>;
%ence(N) -> encn(N, 2, <<1:1>>).

ence({0, 0, R}) -> <<0:1, 0:2, (ence(R))/bits>>;
ence({0, L, 0}) -> <<0:1, 1:2, (ence(L))/bits>>;
ence({0, L, R}) -> <<0:1, 2:2, (ence(L))/bits, (ence(R))/bits>>;
ence({N, 0, R}) -> <<0:1, 3:2, 0:1, 0:1,
                 (ence(N))/bits, (ence(R))/bits>>;
ence({N, L, 0}) -> <<0:1, 3:2, 0:1, 1:1,
                 (ence(N))/bits, (ence(L))/bits>>;
ence({N, L, R}) -> <<0:1, 3:2, 1:1,
                 (ence(N))/bits, (ence(L))/bits, (ence(R))/bits>>;
ence(N) -> encn(N, 2).

dece(<<0:1, 0:2, B/bits>>) -> {R, B1} = dece(B), {{0,0,R}, B1};
dece(<<0:1, 1:2, B/bits>>) -> {L, B1} = dece(B), {{0,L,0}, B1};
dece(<<0:1, 2:2, B/bits>>) -> {L, B1} = dece(B), {R, B2} = dece(B1), {{0,L,R}, B2};
dece(<<0:1, 3:2, 0:1, 0:1, B/bits>>) ->
{N, B1} = dece(B), {R, B2} = dece(B1), {{N, 0, R}, B2};
dece(<<0:1, 3:2, 0:1, 1:1, B/bits>>) ->
{N, B1} = dece(B), {L, B2} = dece(B1), {{N, L, 0}, B2};
dece(<<0:1, 3:2, 1:1, B/bits>>) ->
{N, B1} = dece(B), {L, B2} = dece(B1), {R, B3} = dece(B2), {{N, L, R}, B3};
dece(<<1:1, B/bits>>) -> decn(B, 2, 0).

encn(N, B) when N < (1 bsl B) -> <<((1 bsl B) - 2):B, N:B>>;
encn(N, B) -> encn(N - (1 bsl B), B + 1).

decn(<<0:1, R/bits>>, B, Acc) -> <<N:B, R1/bits>> = R, {N+Acc, R1};
decn(<<1:1, R/bits>>, B, Acc) -> decn(R, B+1, Acc + (1 bsl B)).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

len(D) -> size(encode(D)).

str({I, E}) -> [lists:flatten(stri(I)), lists:flatten(stre(E))].

stri(0) -> "0";
stri(1) -> "";
stri({0, I}) -> "R"++stri(I);
stri({I, 0}) -> "L"++stri(I);
stri({L, R}) -> ["(L"++stri(L), "+", "R"++stri(R), ")"].

stre({N, L, 0}) -> [stre(N), "L", stre(L)];
stre({N, 0, R}) -> [stre(N), "R", stre(R)];
stre({N, L, R}) -> [stre(N), "(L", stre(L), "+R", stre(R), ")"];
stre(N) when N > 0 -> integer_to_list(N);
stre(_) -> "".




