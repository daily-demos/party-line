import { useEffect, useState, useRef } from "react";
import styled from "styled-components";
import { useCallState } from "../CallProvider";
import theme from "../theme";

const Counter = () => {
  const { roomExp, leaveCall, view } = useCallState();
  const [counter, setCounter] = useState("");
  const interval = useRef();

  useEffect(() => {
    /**
     * Rooms exist for 10 minutes from creation.
     * We use the expiry timestamp to show participants
     * how long they have left in the room.
     */
    if (interval.current) {
      clearInterval(interval.current);
    }

    interval.current = setInterval(() => {
      let secs = Math.round((roomExp - Date.now()) / 1000);
      const value = Math.floor(secs / 60) + ":" + ("0" + (secs % 60)).slice(-2);
      if (secs <= 0) {
        clearInterval(interval.current);
        console.log("Eep! Room has expired");
        leaveCall();
        return;
      }
      setCounter(value);
    }, 1000);

    return () => {
      clearInterval(interval.current);
    };
  }, [roomExp, leaveCall, view]);

  return (
    <Container>
      Demo ends in <Count>{counter}</Count>
    </Container>
  );
};

const Container = styled.p`
  font-size: ${theme.fontSize.small};
  color: ${theme.colors.greyDark};
  margin: 0;
  display: flex;
`;
const Count = styled.span`
  width: 28px;
  display: inline-block;
  text-align: right;
`;

export default Counter;
