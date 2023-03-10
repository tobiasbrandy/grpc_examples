package greeter

import (
	"context"
	hw "github.com/tobiasbrandy/grpc_examples/helloworld"
	"google.golang.org/grpc"
	"io"
	"strings"
)

func Register(s grpc.ServiceRegistrar) {
	hw.RegisterGreeterServer(s, &service{})
}

type service struct {
	hw.UnimplementedGreeterServer
}

// SayHello implements helloworld.GreeterServer
func (s *service) SayHello(_ context.Context, request *hw.HelloRequest) (*hw.HelloReply, error) {
	return &hw.HelloReply{Message: "Hello " + request.GetName()}, nil
}

func (s *service) SayHelloRepeated(request *hw.HelloRequest, server hw.Greeter_SayHelloRepeatedServer) error {
	const repetitions int = 5

	reply := &hw.HelloReply{Message: "Hello " + request.GetName()}
	for i := 0; i < repetitions; i++ {
		if err := server.Send(reply); err != nil {
			return err
		}
	}
	return nil
}

func (s *service) SayHelloMany(server hw.Greeter_SayHelloManyServer) error {
	sb := strings.Builder{}
	sb.WriteString("Hello ")
	init := true

	for {
		req, err := server.Recv()
		if err == io.EOF {
			return server.SendAndClose(&hw.HelloReply{Message: sb.String()})
		}
		if err != nil {
			return err
		}
		if !init {
			sb.WriteString(", ")
		}
		sb.WriteString(req.GetName())

		init = false
	}
}

func (s *service) SayHelloManyMany(server hw.Greeter_SayHelloManyManyServer) error {
	for {
		req, err := server.Recv()
		if err == io.EOF {
			return nil
		}
		if err != nil {
			return err
		}
		if err := server.Send(&hw.HelloReply{Message: "Hello " + req.String()}); err != nil {
			return err
		}
	}
}
