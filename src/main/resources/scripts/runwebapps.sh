#!/bin/bash -x

function run_webapp() {
    if [ ! -z "$TMUX" ]; then
        tmux new-window -n $1 "$2"; [ $? -eq 0 -o $? -eq 143 ] || sh
    else
        xterm -T $1 -e "$2"; [ $? -eq 0 -o $? -eq 143 ] || sh
    fi;
}
