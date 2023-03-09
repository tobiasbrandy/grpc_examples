package greeter

import (
	"context"
	hw "github.com/tobiasbrandy/grpc_examples/helloworld"
	"google.golang.org/grpc"
)

func Register(s grpc.ServiceRegistrar) {
	hw.RegisterGreeterServer(s, &service{})
}

type service struct {
	hw.UnimplementedGreeterServer
}

// SayHello implements helloworld.GreeterServer
func (s *service) SayHello(_ context.Context, in *hw.HelloRequest) (*hw.HelloReply, error) {
	return &hw.HelloReply{Message: "Hello " + in.GetName()}, nil
}
