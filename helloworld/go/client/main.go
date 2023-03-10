package main

import (
	"context"
	"flag"
	"io"
	"log"
	"os"
	"time"

	hw "github.com/tobiasbrandy/grpc_examples/helloworld"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

var (
	addr = flag.String("addr", "localhost:50051", "the address to connect to")

	infoLog = log.New(os.Stdout, "INFO ", log.LstdFlags|log.Lshortfile|log.Lmsgprefix)
	errLog  = log.New(os.Stderr, "ERROR ", log.LstdFlags|log.Lshortfile|log.Lmsgprefix)

	timeout = 10 * time.Second
	names   = []string{"World", "Steve", "Tobias", "Martin", "Sarah"}
)

func SayHello(client hw.GreeterClient, name string) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	res, err := client.SayHello(ctx, &hw.HelloRequest{Name: name})
	if err != nil {
		errLog.Printf("SayHello() RPC error: %v", err)
		return
	}

	infoLog.Print(res.GetMessage())
}

func SayHelloRepeated(client hw.GreeterClient, name string) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	stream, err := client.SayHelloRepeated(ctx, &hw.HelloRequest{Name: name})
	if err != nil {
		errLog.Printf("SayHelloRepeated() RPC error: %v", err)
		return
	}

	for {
		res, err := stream.Recv()
		if err == io.EOF {
			return
		}
		if err != nil {
			errLog.Printf("SayHelloRepeated() RPC error: %v", err)
			return
		}

		infoLog.Print(res.GetMessage())
	}
}

func SayHelloMany(client hw.GreeterClient, names []string) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	stream, err := client.SayHelloMany(ctx)
	if err != nil {
		errLog.Printf("SayHelloMany() RPC error: %v", err)
		return
	}

	for _, name := range names {
		if err := stream.Send(&hw.HelloRequest{Name: name}); err != nil {
			errLog.Printf("SayHelloMany() RPC error: %v", err)
			return
		}
		time.Sleep(1 * time.Second)
	}

	res, err := stream.CloseAndRecv()
	if err != nil {
		errLog.Printf("SayHelloMany() RPC error: %v", err)
		return
	}

	infoLog.Print(res.GetMessage())
}

func SayHelloManyMany(client hw.GreeterClient, names []string) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	stream, err := client.SayHelloManyMany(ctx)
	if err != nil {
		errLog.Printf("SayHelloManyMany() RPC error: %v", err)
		return
	}

	waitResponses := make(chan struct{})
	go func() {
		for {
			res, err := stream.Recv()
			if err == io.EOF {
				close(waitResponses)
				return
			}
			if err != nil {
				errLog.Printf("SayHelloManyMany() RPC error: %v", err)
			}
			infoLog.Print(res.GetMessage())
		}
	}()

	func() {
		defer func() {
			if err := stream.CloseSend(); err != nil {
				errLog.Printf("SayHelloManyMany() RPC error: %v", err)
			}
		}()

		for _, name := range names {
			if err := stream.Send(&hw.HelloRequest{Name: name}); err != nil {
				errLog.Printf("SayHelloManyMany() RPC error: %v", err)
				return
			}
			time.Sleep(1 * time.Second)
		}
	}()

	<-waitResponses
}

func main() {
	flag.Parse()

	conn, err := grpc.Dial(*addr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		errLog.Fatalf("did not connect: %v", err)
	}
	defer func(conn *grpc.ClientConn) {
		err := conn.Close()
		if err != nil {
			errLog.Printf("Error while closing connection: %v", err)
		}
	}(conn)

	client := hw.NewGreeterClient(conn)

	infoLog.Print("------- SayHello -------")
	SayHello(client, names[0])

	infoLog.Print("------- SayHelloRepeated -------")
	SayHelloRepeated(client, names[0])

	infoLog.Print("------- SayHelloMany -------")
	SayHelloMany(client, names)

	infoLog.Print("------- SayHelloManyMany -------")
	SayHelloManyMany(client, names)
}
