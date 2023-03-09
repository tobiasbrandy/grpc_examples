package main

import (
	"flag"
	"fmt"
	"github.com/tobiasbrandy/grpc_examples/server/greeter"
	"google.golang.org/grpc/reflection"
	"log"
	"net"

	"google.golang.org/grpc"
)

var serviceRegistrations = []func(grpc.ServiceRegistrar){
	greeter.Register,
}

func Server() *grpc.Server {
	s := grpc.NewServer()
	for _, register := range serviceRegistrations {
		register(s)
	}
	reflection.Register(s) // Allow reflection
	return s
}

var (
	port = flag.Int("port", 50051, "The server port")
)

func main() {
	flag.Parse()

	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", *port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}

	s := Server()
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}

	log.Printf("server listening at %v", lis.Addr())
}
