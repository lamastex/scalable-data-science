{-# LANGUAGE TemplateHaskell #-}
module Notebook where

import Data.Default (def)
import qualified Data.Default as D
import Data.Text (Text, unpack)
import qualified Data.ByteString.Lazy as B
import qualified Data.HashMap.Lazy as H
import Control.Lens hiding ((.:))

data Notebook = N { _nName     :: Text
                  , _nCommands :: [Command] }
  deriving (Eq, Show)

data Command = C { _cLanguage :: Text
                 , _cCommand  :: Text }
  deriving (Eq, Show)

makeLenses ''Notebook
makeLenses ''Command

-- data Language = Java
--               | R
--               | Scala
--               | Python
--               | Haskell
--               | Other Text
--               deriving (Eq, Show)
