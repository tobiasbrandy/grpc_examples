package main

import (
	"context"
	"flag"
	"log"
	"time"

	hw "github.com/tobiasbrandy/grpc_examples/helloworld"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

const (
	defaultName = "world"
)

var (
	addr = flag.String("addr", "localhost:50051", "the address to connect to")
	name = flag.String("name", defaultName, "Name to greet")
)

func main() {
	flag.Parse()

	conn, err := grpc.Dial(*addr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer func(conn *grpc.ClientConn) {
		err := conn.Close()
		if err != nil {
			log.Printf("Error while closing connection: %v", err)
		}
	}(conn)

	c := hw.NewGreeterClient(conn)

	ctx, cancel := context.WithTimeout(context.Background(), 1*time.Second)
	defer cancel()

	r, err := c.SayHello(ctx, &hw.HelloRequest{Name: *name})
	if err != nil {
		log.Fatalf("SayHello() RPC error: %v", err)
	}

	log.Printf("Greeting: %s", r.GetMessage())
}
