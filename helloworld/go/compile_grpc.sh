#!/usr/bin/env bash

# Use relative paths to the script location
SCRIPT_DIR=$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

protoc --proto_path="$SCRIPT_DIR"/../proto \
 --go_out="$SCRIPT_DIR" --go_opt=module=github.com/tobiasbrandy/grpc_examples \
 --go-grpc_out="$SCRIPT_DIR" --go-grpc_opt=module=github.com/tobiasbrandy/grpc_examples \
 "$SCRIPT_DIR"/../proto/*
