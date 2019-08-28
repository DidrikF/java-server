import React, {useState, useEffect } from 'react';
import axios from 'axios';


function JokeComponent() {
    const [jokes, setJokes] = useState([]);
    const [message, setMessage] = useState("");

    useEffect(() => {
        async function fetchJokes() {
            try {
                const response = await axios.get("/jokes");
                console.log("Response: ", response);
                setJokes(response.data.list);
            } catch(e) {
                console.log("Failed to get jokes")
                
                // setJokes(defaultJokes);
            }
        }
        fetchJokes()
    }, []);

    function flashMessage(message, seconds) {
        setMessage(message)
        setTimeout(() => {
            setMessage("");
        }, seconds * 1000);
    }

    return (
        <div>
            <table className="Joke__container">
                <thead>
                    <tr>
                        <th>Author</th>
                        <th>Joke</th>
                        <th>Votes</th>
                    </tr>
                </thead>
                <tbody>
                    {jokes.map((joke, index) => {
                        return (
                            <JokeCard key={index} index={index} joke={joke} falshMessage={flashMessage} setJokes={setJokes} />
                        )
                    })}
                </tbody>
            </table>
            <JokeForm flashMessage={flashMessage} setJokes={setJokes}/>
            <div className="Joke__message">
                {message}
            </div>
        </div>
    );
}


function JokeCard(props) {
    
    async function vote(index, value) {
        try {
            const response = await axios.post("/votes", {
                "index": index,
                "vote": value
            });
            props.setJokes(response.data.list);
        } catch(e) {
            console.log("Failed to vote");
            props.flashMessage("Failed to vote");
        }
    }

    console.log("Joke in joke card: ", props.joke);
    return (
        <tr className="Joke__card">
            <td className="Joke__card-author">
                {props.joke.author}
            </td>
            <td className="Joke__card-joke">
                {props.joke.joke}
            </td>
            <td className="Joke__card-votes">
                {props.joke.votes}
                <button onClick={(e) => vote(props.index, 1)}>Up</button>
                <button onClick={(e) => vote(props.index, -1)}>Down</button>
            </td>
        </tr>
    )
}

function JokeForm (props) {
    const [newJoke, setNewJoke] = useState("");
    const [jokeAuthor, setJokeAuthor] = useState("");

    async function postJoke(e) {
        e.preventDefault();
        const jokeObject = {
            author: jokeAuthor,
            votes: 0,
            joke: newJoke
        }
        try {
            const response = await axios.post("/jokes", jokeObject);
            if (response.status === 201) {
                props.flashMessage("Successfully posted joke!");
                props.setJokes(response.data.list)
            } else {
                props.flashMessage("Failed to post joke.");
            }
        } catch (e) {
            props.flashMessage("Failed to post joke.")
        }
    }

    return (
        <form className="Joke__form">
            <div className="Joke__form-group">
                <label>
                    Write your joke
                    <input className="Joke__form-input" value={newJoke} onChange={e => setNewJoke(e.target.value)}/>
                </label>
            </div>
            <div className="Joke__form-group">
                <label>
                    and your name
                    <input className="Joke__form-input" value={jokeAuthor} onChange={e => setJokeAuthor(e.target.value)}/>
                </label>
            </div>

            <button className="Joke__button--post Joke__button" onClick={postJoke}>Post Joke</button>
        </form>
    )
}


export default JokeComponent;




/*
const defaultJokes = [
    {
        author: "Peter Johnson",
        votes: 0,
        joke: "Today at the bank, an old lady asked me to help check her balance. So I pushed her over.",
    }, 
    {
        author: "Kate Rash",
        votes: 0,
        joke: "I bought some shoes from a drug dealer. I don't know what he laced them with, but I've been tripping all day.",
    }, 
    {
        author: "John Doe",
        votes: 0,
        joke: "I told my girlfriend she drew her eyebrows too high. She seemed surprised.",
    },
    {
        author: "Pete Holm",
        votes: 0,
        joke: "My dog used to chase people on a bike a lot. It got so bad, finally I had to take his bike away.",
    },
]

*/